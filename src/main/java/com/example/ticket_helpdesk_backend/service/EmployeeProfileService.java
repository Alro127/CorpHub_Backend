package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.BucketName;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeProfileService {
    @Autowired
    EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    DocumentTypeRepository documentTypeRepository;

    @Autowired
    EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    UserService userService;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    CompetencyTypeRepository competencyTypeRepository;

    @Autowired
    EmployeeCompetencyService employeeCompetencyService;

    @Autowired
    PositionRepository positionRepository;

    @Autowired
    EmployeeAdministrativeInfoRepository employeeAdministrativeInfoRepository;

    @Autowired
    EmployeeDocumentService employeeDocumentService;

    @Autowired
    InternalWorkHistoryService internalWorkHistoryService;

    @Autowired
    PositionChangeRequestService positionChangeRequestService;

    @Autowired
    JwtUtil jwtUtil;

    @Transactional
    public EmployeeProfile createEmployeeProfile(CreateEmployeeProfileRequest request) throws ResourceNotFoundException {

        // ====== Tạo hồ sơ nhân viên ======
        EmployeeProfile employeeProfile = new EmployeeProfile();
        employeeProfile.setFullName(request.getFullName());
        employeeProfile.setDob(request.getDob());
        employeeProfile.setGender(request.getGender());
        employeeProfile.setPhone(request.getPhone());
        employeeProfile.setPersonalEmail(request.getPersonalEmail());
        employeeProfile.setJoinDate(request.getJoinDate());
        employeeProfile.setCode(generateEmployeeCode(request.getFullName()));

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employeeProfile.setDepartment(dept);
        }

        // Chức danh mặc định khi vừa được tạo hồ sơ nhân viên
        Position position = positionRepository.findFirstByDepartmentIdOrderByLevelOrderAsc(request.getDepartmentId());
        if (request.getPositionId() != null) {
            position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found"));
        }

        employeeProfile.setPosition(position);

        // ====== Lưu EmployeeProfile ======
        return employeeProfileRepository.save(employeeProfile);

    }

    public String generateEmployeeCode(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required to generate employee code");
        }

        // Chuẩn hóa khoảng trắng
        String normalized = fullName.trim().replaceAll("\\s+", " ");

        // Bỏ dấu tiếng Việt
        String noAccent = removeVietnameseAccent(normalized);

        String[] parts = noAccent.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Full name must include at least first name and last name");
        }

        // Last name (Tên)
        String lastName = capitalize(parts[parts.length - 1]);

        // Initial của họ + tên lót
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            initials.append(Character.toUpperCase(parts[i].charAt(0)));
        }

        String baseCode = lastName + initials;

        // Đếm số code đã tồn tại
        long count = employeeProfileRepository.countByCodeStartingWith(baseCode);

        // Trả về code cuối
        return count == 0 ? baseCode : baseCode + count;
    }

    private String removeVietnameseAccent(String input) {
        if (input == null) return null;

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccent = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Xử lý riêng chữ Đ/đ
        return withoutAccent
                .replace("Đ", "D")
                .replace("đ", "d");
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

public Page<EmployeeProfileResponse> getAllEmployeeProfiles(int page, int size, String keyword) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fullName"));

    Page<EmployeeProfile> pageResult;

    if (keyword != null && !keyword.isEmpty()) {
        pageResult = employeeProfileRepository.findByFullNameContainingIgnoreCase(keyword, pageable);
    } else {
        pageResult = employeeProfileRepository.findAll(pageable);
    }

    return pageResult.map(profile -> {
        String avatarUrl = null;
        if (profile.getAvatar() != null) {
            avatarUrl = fileStorageService.getPresignedUrl("employee-avatars", profile.getAvatar());
        }
        return EmployeeProfileResponse.fromEntity(profile, avatarUrl);
    });
}



    public List<EmployeeProfile> getEmployeesByDepartment(UUID departmentId) {
        return employeeProfileRepository.findByDepartment_Id(departmentId);
    }

    public EmployeeProfile getEmployeeProfileById(UUID id) {
        return employeeProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional
    public boolean uploadAvatar(String token, MultipartFile avatarFile) throws ResourceNotFoundException {

        UUID userId = jwtUtil.getUserId(token);

        EmployeeProfile employeeProfile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee không tồn tại"));

        String fileUrl;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            fileUrl = fileStorageService.uploadFile(BucketName.EMPLOYEE_AVATAR.getBucketName(), avatarFile, employeeProfile.getFullName());
        } else {
            ClassPathResource defaultAvatar = new ClassPathResource("public/images/defaultAvatar.jpg");
            try (InputStream inputStream = defaultAvatar.getInputStream()) {
                fileUrl = fileStorageService.uploadFile(BucketName.EMPLOYEE_AVATAR.getBucketName(), inputStream, "default.jpg", employeeProfile.getFullName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        employeeProfile.setAvatar(fileUrl);

        // ====== Lưu EmployeeProfile ======
        employeeProfileRepository.save(employeeProfile);

        return true;
    }

    public EmployeeProfile getMyEmployeeProfile(String token) {
        UUID userId = jwtUtil.getUserId(token);
        

        return employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    private EmployeeProfile getProfile(String token) {
        UUID userId = jwtUtil.getUserId(token);
        return employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public EmployeeProfileResponse getBasicProfile(String token) {
        EmployeeProfile profile = getProfile(token);
        String avatar = fileStorageService.getPresignedUrl(BucketName.EMPLOYEE_AVATAR.getBucketName(), profile.getAvatar());
        return EmployeeProfileResponse.fromEntity(profile, avatar);
    }

    public List<EmployeeJobHistoryResponse> getMyJobHistories(String token) {
        return getProfile(token).getJobHistories().stream()
                .map(job -> new EmployeeJobHistoryResponse(
                        job.getId(),
                        job.getContractType(),
                        job.getStartDate(),
                        job.getEndDate(),
                        job.getEmploymentStatus(),
                        job.getNote()
                )).toList();
    }

    public List<EmployeeCompetencyDto> getMyCompetencies(String token) {
        return getProfile(token).getCompetencies().stream()
                .map(EmployeeCompetencyDto::fromEntity).toList();
    }

    public List<EmployeeDocumentResponse> getMyDocuments(String token) {
        return getProfile(token).getDocuments().stream()
                .map(EmployeeDocumentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EmployeeContactInfoUpdateDto updateMyContactInfo(String token, EmployeeContactInfoUpdateDto request) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);

        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for current user"));

        // Validate
        validateContactInfo(request);

        profile.setPersonalEmail(request.getPersonalEmail());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setAbout(request.getAbout());

        return EmployeeContactInfoUpdateDto.fromEntity(employeeProfileRepository.save(profile));
    }

    private void validateContactInfo(EmployeeContactInfoUpdateDto request) {
        // Ví dụ: validate phone VN đơn giản
        if (request.getPhone() != null && !request.getPhone().matches("^[0-9+]{8,20}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    @Transactional
    public EmployeeAdministrativeInfo updateAdministrativeInfo(UUID employeeId,
                                                               EmployeeAdministrativeInfoDto request) throws ResourceNotFoundException {

        EmployeeProfile profile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        EmployeeAdministrativeInfo info = employeeAdministrativeInfoRepository.findByEmployeeProfileId(employeeId)
                .orElseGet(() -> {
                    EmployeeAdministrativeInfo newInfo = new EmployeeAdministrativeInfo();
                    newInfo.setEmployeeProfile(profile);
                    return newInfo;
                });

        if (request.getIdentityIssuedDate() != null &&
                request.getIdentityIssuedDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Identity issued date cannot be in the future");
        }

        info.setIdentityNumber(request.getIdentityNumber());
        info.setIdentityIssuedDate(request.getIdentityIssuedDate());
        info.setIdentityIssuedPlace(request.getIdentityIssuedPlace());
        info.setTaxCode(request.getTaxCode());
        info.setSocialInsuranceNumber(request.getSocialInsuranceNumber());
        info.setBankAccountNumber(request.getBankAccountNumber());
        info.setBankName(request.getBankName());
        info.setMaritalStatus(request.getMaritalStatus());
        info.setNote(request.getNote());

        return employeeAdministrativeInfoRepository.save(info);
    }

    @Transactional
    public EmployeeProfile updateBasicInfo(UUID employeeId, HrEmployeeBasicInfoUpdateRequest request) throws ResourceNotFoundException {
        EmployeeProfile profile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        profile.setFullName(request.getFullName());
        profile.setDob(request.getDob());
        profile.setGender(request.getGender());

        // thêm validate dob nếu cần
        if (request.getDob() != null && request.getDob().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        return employeeProfileRepository.save(profile);
    }

    @Transactional
    public EmployeeProfile updateContactInfo(UUID employeeId, HrEmployeeContactInfoUpdateRequest request) throws ResourceNotFoundException {
        EmployeeProfile profile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (request.getPersonalEmail() != null) {
            profile.setPersonalEmail(request.getPersonalEmail());
        }
        if (request.getPhone() != null) {
            if (!request.getPhone().matches("^[0-9+]{8,20}$")) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            profile.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getAbout() != null) {
            profile.setAbout(request.getAbout());
        }

        return employeeProfileRepository.save(profile);
    }

    public EmployeeFullDetailResponse getEmployeeFullDetail(UUID employeeId) throws ResourceNotFoundException {

        EmployeeProfile profile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Avatar presigned URL
        String avatarUrl = null;
        if (profile.getAvatar() != null) {
            avatarUrl = fileStorageService.getPresignedUrl("employee-avatars", profile.getAvatar());
        }

        // Administrative Info
        EmployeeAdministrativeInfo admin = profile.getAdministrativeInfo();
        var adminDto = EmployeeAdministrativeInfoDto.fromEntity(admin);

        // Competencies
        List<EmployeeCompetencyResponse> competencies =
                employeeCompetencyService.getByEmployeeId(employeeId);

        // Documents
        List<EmployeeDocumentResponse> documents =
                employeeDocumentService.getByEmployeeId(employeeId);

        for (var document : documents) {
            document.setFileUrl(fileStorageService.getPresignedUrl(BucketName.EMPLOYEE_DOCUMENT.getBucketName(),document.getFileUrl()));
        }

        // Internal Work History
        var internalHistories = internalWorkHistoryService.getByEmployee(employeeId);

        // Position Change Requests
        var positionRequests = positionChangeRequestService.getRequestsByEmployee(employeeId);

        // Build DTO
        EmployeeFullDetailResponse dto = new EmployeeFullDetailResponse();

        // --- Personal info ---
        dto.setId(profile.getId());
        dto.setCode(profile.getCode());
        dto.setFullName(profile.getFullName());
        dto.setAvatarUrl(avatarUrl);
        dto.setGender(profile.getGender());
        dto.setDob(profile.getDob());
        dto.setPhone(profile.getPhone());
        dto.setPersonalEmail(profile.getPersonalEmail());
        dto.setAddress(profile.getAddress());
        dto.setAbout(profile.getAbout());
        dto.setJoinDate(profile.getJoinDate());

        // --- Job info ---
        dto.setPositionId(profile.getPosition() != null ? profile.getPosition().getId() : null);
        dto.setPositionName(profile.getPosition() != null ? profile.getPosition().getName() : null);

        dto.setDepartmentId(profile.getDepartment() != null ? profile.getDepartment().getId() : null);
        dto.setDepartmentName(profile.getDepartment() != null ? profile.getDepartment().getName() : null);

        dto.setManagerId(profile.getDepartment().getManager() != null ? profile.getDepartment().getManager().getId() : null);
        dto.setManagerName(profile.getDepartment().getManager() != null ? profile.getDepartment().getManager().getFullName() : null);

        dto.setActive(profile.getUser() != null ? profile.getUser().getActive() : null);

        // --- Administrative info ---
        dto.setAdministrativeInfo(adminDto);

        // --- Collections ---
        dto.setCompetencies(competencies);
        dto.setDocuments(documents);
        dto.setInternalHistories(internalHistories);
        dto.setPositionChangeRequests(positionRequests);

        return dto;
    }
}
