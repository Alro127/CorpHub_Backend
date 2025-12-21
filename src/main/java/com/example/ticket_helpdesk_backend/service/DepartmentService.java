package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    FileStorageService fileStorageService;

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

    public List<DepartmentDto> getDepartmentDtoList() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentDto.class))
                .toList();
    }

    @Cacheable(value = "usersByDepartment", key = "#departmentId")
    public List<UserDto> getUsersByDepartment(UUID userId) throws ResourceNotFoundException {
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getEmployeeProfile().getDepartment().getName() == null) {
            throw new ResourceNotFoundException("User with id " + userId + " has no department assigned");
        }

        UUID departmentId = user.getEmployeeProfile().getDepartment().getId();

        List<UserDto> usersDepartmentList = userRepository.findByEmployeeProfile_Department_Id(departmentId)
                .stream()
                .map(UserDto::toUserDto)
                .toList();

        if (usersDepartmentList.isEmpty()) {
            throw new ResourceNotFoundException("No users found for department " + departmentId);
        }
        return usersDepartmentList;
    }


    private static final String EMPLOYEE_BUCKET = "employee-avatars"; // bucket chứa ảnh nhân viên

    public List<DepartmentDetailDto> getAllDepartmentsWithUsers() {
        List<Department> all = departmentRepository.findAllWithUsers();

        return all.stream()
                .filter(d -> d.getParent() == null)
                .map(this::mapDepartment)
                .toList();
    }

    // Mapping từ EmployeeProfile sang UserDepartmentDto để phục vụ cho việc quản lý phòng ban
    private UserDepartmentDto mapEmployee(EmployeeProfile ep) {
        if (ep == null || ep.getUser() == null) return null;

        String avatarKey = ep.getAvatar();
        String avatarUrl = null;

        if (avatarKey != null && !avatarKey.isEmpty()) {
            avatarUrl = fileStorageService.getPresignedUrl(EMPLOYEE_BUCKET, avatarKey);
        }

        UUID positionId = null;
        String positionName = null;
        Integer levelOrder = null;
        if (ep.getPosition() != null) {
            positionId = ep.getPosition().getId();
            positionName = ep.getPosition().getName();
            levelOrder = ep.getPosition().getLevelOrder();
        }

        return UserDepartmentDto.builder()
                .userId(ep.getUser().getId())
                .fullName(ep.getFullName())
                .email(ep.getUser().getUsername())
                .avatar(avatarUrl)
                .positionId(positionId)
                .positionName(positionName)
                .levelOrder(levelOrder)
                .build();
    }

    private DepartmentDetailDto mapDepartment(Department dept) {

        // Map users
        List<UserDepartmentDto> users = dept.getEmployeeProfiles().stream()
                .map(this::mapEmployee)
                .filter(Objects::nonNull)
                .toList();

        // Map manager
        UserDepartmentDto managerDto = mapEmployee(dept.getManager());

        // Map children (ĐỆ QUY)
        List<DepartmentDetailDto> childDtos = dept.getChildren().stream()
                .map(this::mapDepartment)
                .toList();

        return DepartmentDetailDto.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .manager(managerDto)
                .users(users)
                .children(childDtos)
                .build();
    }


    public DepartmentManagementDto createDepartment(DepartmentManagementDto dto) throws ResourceNotFoundException {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }

        EmployeeProfile managerEntity = null;
        if (dto.getManager() != null && dto.getManager().getId() != null) {
            managerEntity = employeeProfileRepository.findById(dto.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        Department saved = departmentRepository.save(dto.toEntity(managerEntity));
        return DepartmentManagementDto.fromEntity(saved);
    }

    public DepartmentManagementDto updateDepartment(UUID id, DepartmentManagementDto dto) throws ResourceNotFoundException {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());

        if (dto.getManager() != null && dto.getManager().getId() != null) {
            EmployeeProfile manager = employeeProfileRepository.findById(dto.getManager().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            existing.setManager(manager);
        }

        Department updated = departmentRepository.save(existing);
        return DepartmentManagementDto.fromEntity(updated);
    }

    public void deleteDepartment(UUID id) throws ResourceNotFoundException {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    @Transactional
    public DepartmentManagementDto assignManager(UUID departmentId, UUID managerId)
            throws ResourceNotFoundException {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        EmployeeProfile manager = employeeProfileRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // 1️⃣ Check trạng thái
        if (!manager.getUser().getActive()) {
            throw new RuntimeException("Employee is not active, cannot assign as manager");
        }

        // 2️⃣ khác phòng ban
        if (!manager.getDepartment().getId().equals(departmentId)) {
            // log warning / audit
            throw new RuntimeException("Assign manager from different department");
        }

        department.setManager(manager);
        Role role = roleRepository.findByName(UserRole.ROLE_MANAGER.name()).orElseThrow();
        manager.getUser().setRole(role);
        return DepartmentManagementDto.fromEntity(departmentRepository.save(department));
    }


    @Transactional
    public void moveDepartment(UUID dragId, UUID newParentId) throws ResourceNotFoundException {

        Department drag = departmentRepository.findById(dragId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dragId));

        Department newParent = null;

        if (newParentId != null) {
            newParent = departmentRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + newParentId));

            // ❌ 1. Không được move vào chính nó
            if (drag.getId().equals(newParent.getId())) {
                throw new IllegalArgumentException("Cannot move a department into itself.");
            }

            // ❌ 2. Không được move vào con của chính nó → tạo vòng lặp
            if (isDescendant(drag, newParent)) {
                throw new IllegalArgumentException("Cannot move department into its descendant.");
            }
        }

        // ✔ 3. Set parent mới
        drag.setParent(newParent);
        departmentRepository.save(drag);
    }


    // ====== Helper: check xem parentNew có phải con của drag không ======
    private boolean isDescendant(Department parent, Department child) {
        if (child.getParent() == null) return false;
        if (child.getParent().getId().equals(parent.getId())) return true;
        return isDescendant(parent, child.getParent());
    }

    @Transactional
    public List<DepartmentPositionDto> getDepartmentWithPosition() {

        // Lấy tất cả phòng ban
        List<Department> departments = departmentRepository.findAll();

        return departments.stream()
                .map(department -> {
                    // Lấy positions thuộc department này
                    List<Position> positions = positionRepository.findByDepartmentIdOrderByLevelOrderAsc(department.getId());

                    return toDepartmentPositionDto(department, positions);
                })
                .toList();
    }

    private DepartmentPositionDto toDepartmentPositionDto(
            Department department,
            List<Position> positions
    ) {
        DepartmentPositionDto dto = new DepartmentPositionDto();
        dto.setDepartmentId(department.getId());
        dto.setDepartmentName(department.getName());

        List<PositionInDepartmentDto> mappedPositions = positions.stream()
                .map(p -> {
                    PositionInDepartmentDto pd = new PositionInDepartmentDto();
                    pd.setId(p.getId());
                    pd.setName(p.getName());
                    pd.setCode(p.getCode());
                    pd.setDescription(p.getDescription());
                    pd.setLevelOrder(p.getLevelOrder());
                    return pd;
                })
                .toList();

        dto.setPositions(mappedPositions);
        return dto;
    }




//    public List<DepartmentManagementDto> getAllDepartments() {
//        return departmentRepository.findAll().stream()
//                .map(DepartmentManagementDto::fromEntity)
//                .collect(Collectors.toList());
//    }
}
