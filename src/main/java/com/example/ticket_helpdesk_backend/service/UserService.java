package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.specification.UserSpecifications;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.ticket_helpdesk_backend.specification.TicketSpecifications.*;
import static com.example.ticket_helpdesk_backend.specification.TicketSpecifications.createdBetween;
import static com.example.ticket_helpdesk_backend.specification.TicketSpecifications.hasCategory;
import static com.example.ticket_helpdesk_backend.specification.TicketSpecifications.search;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    FileStorageService fileStorageService;

    private static final String BUCKET_NAME = "employee-avatars";

    // Mapping FE -> Entity path
    private static final Map<String, String> SORT_FIELD_MAP = Map.ofEntries(
            Map.entry("fullName", "employeeProfile.fullName"),
            Map.entry("email", "username"),
            Map.entry("gender", "employeeProfile.gender"),
            Map.entry("department", "employeeProfile.department.name"),
            Map.entry("active", "active")
    );


    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    ModelMapper modelMapper;

    public User getUserFromToken(String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    @Transactional(readOnly = true)
    public List<NameInfoDto> getNameInfo() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, NameInfoDto.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserDtoByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserDto::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + username));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUser(Specification<User> spec ,Pageable pageable) {
        return userRepository.findAll(spec, pageable)
                .map(user -> {
                    // map các field cơ bản
                    UserDto dto = UserDto.toUserDto(user);

                    // xử lý avatarUrl nếu có avatar
                    if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
                        dto.setAvatar(
                                fileStorageService.getPresignedUrl(BUCKET_NAME, dto.getAvatar())
                        );
                    }

                    return dto;
                });
    }


    @Transactional(readOnly = true)
    public Page<UserDto> getEmployees(String token,
                                      int page,
                                      int size,
                                      String keyword,
                                      String gender,
                                      String departmentId,
                                      Boolean isActive,
                                      String sortField,
                                      String sortDir) throws ResourceNotFoundException {
        User currentUser = getUserFromToken(token);
        String role = jwtUtil.getRole(token);
        UserRole userRole = UserRole.valueOf(role);

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        String sortProperty = SORT_FIELD_MAP.getOrDefault(sortField, "id");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        // ✅ Luôn tạo spec gốc
        Specification<User> spec = Specification.where(UserSpecifications.withEmployeeJoins());

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(UserSpecifications.search(keyword));
            System.out.println("Apply keyword filter: " + keyword);
        }

        if (gender != null && !gender.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> emp = root.join("employeeProfile", JoinType.LEFT);
                return cb.equal(cb.lower(emp.get("gender")), gender.toLowerCase());
            });
            System.out.println("Apply gender filter: " + gender);
        }

        if (departmentId != null && !departmentId.isBlank()) {
            try {
                UUID depId = UUID.fromString(departmentId);
                spec = spec.and(UserSpecifications.belongsToDepartment(depId));
                System.out.println("Apply department filter: " + depId);
            } catch (Exception e) {
                System.out.println("Invalid department ID: " + departmentId);
            }
        }

        if (isActive != null) {
            spec = spec.and(UserSpecifications.isActive(isActive));
            System.out.println("Apply isActive filter: " + isActive);
        }

        if (userRole != UserRole.ROLE_ADMIN) {
            UUID currentDepartmentId = currentUser.getEmployeeProfile().getDepartment().getId();
            spec = spec.and(UserSpecifications.belongsToDepartment(currentDepartmentId));
            System.out.println("Apply department limit: " + currentDepartmentId);
        }

        System.out.println("Final spec null? " + (spec == null));

        Page<UserDto> result = getAllUser(spec, pageable);
        System.out.println("Total records returned: " + result.getTotalElements());

        return result;
    }


    public List<UserDto> getUsersBySearch(String keyword) {
        return userRepository.searchByFullNameOrUsername(keyword).stream().map(UserDto::toUserDto).collect(Collectors.toList());
    }

    @Transactional
    public boolean createUser(CreateUserRequest req) {
        var employee = employeeProfileRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee không tồn tại"));

        if (userRepository.findByUsername(req.getEmail()).isPresent()) {
            throw new RuntimeException("UserName đã tồn tại");
        }

        User user = new User();
        user.setEmployeeProfile(employee);
        user.setUsername(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setActive(false);
        user.setRole(roleRepository.findByName(req.getRole())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại")));
        // Set ngày hết hạn sau 7 ngày
        user.setExpired(LocalDateTime.now().plusDays(7));

        userRepository.save(user);
        return true;
    }

    public User getManagerOfUser(UUID userId) {
        Specification<User> spec = Specification
                .where(UserSpecifications.hasRoleName("ROLE_MANAGER"))
                .and(UserSpecifications.isActive(true))
                .and(UserSpecifications.inSameDepartmentAsUser(userId));

        return userRepository.findAll(spec)
                .stream().findFirst().orElseThrow(() -> new RuntimeException("User dont have manager"));
    }

//    public UserDataResponse getEmployeeById(UUID userId) throws ResourceNotFoundException {
//        if (userId == null) {
//            throw new RuntimeException("Invalid input, user id is null");
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
//
//        Account account = accountRepository.findById(userId).orElse(null);
//
//        // Chuyển đổi entity sang DTO
//        return UserDataResponse.fromEntity(user, account);
//    }

//    public User createUserForEmployee(UUID employeeProfileId, CreateUserRequest request) {
//        EmployeeProfile profile = employeeProfileRepository.findById(employeeProfileId)
//                .orElseThrow(() -> new RuntimeException("EmployeeProfile not found"));
//
//        User user = new User();
//        user.setUsername(request.getUserName());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(roleRepository.findById(request.getRoleId())
//                .orElseThrow(() -> new RuntimeException("Role not found")));
//        user.setActive(true);
//        user.setId(request.getEmployeeId());
//
//        userRepository.save(user);
//
//        profile.setUser(user);
//        employeeProfileRepository.save(profile);
//
//        return user;
//    }


    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private String getRoleName(UUID userId) {
        User user = getUserById(userId);
        return user != null ? user.getRole().getName() : null;
    }

    public boolean isAdmin(UUID userId) {
        return "ROLE_ADMIN".equals(getRoleName(userId));
    }

    public boolean isManager(UUID userId) {
        return "ROLE_MANAGER".equals(getRoleName(userId));
    }

    public boolean isUser(UUID userId) {
        return "ROLE_USER".equals(getRoleName(userId));
    }

}
