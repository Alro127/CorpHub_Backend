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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

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
    JwtUtil jwtUtil;




    @Transactional
    public boolean createEmployeeProfile(CreateEmployeeProfileRequest request, String token) throws ResourceNotFoundException {

        // ====== Tạo hồ sơ nhân viên ======
        EmployeeProfile employeeProfile = new EmployeeProfile();
        employeeProfile.setFullName(request.getFullName());
        employeeProfile.setDob(request.getDob());
        employeeProfile.setGender(request.getGender());
        employeeProfile.setPhone(request.getPhone());
        employeeProfile.setPersonalEmail(request.getPersonalEmail());
        employeeProfile.setAvatar(request.getAvatar());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employeeProfile.setDepartment(dept);
        }

        // ====== JobHistories ======
        if (request.getJobHistories() != null && !request.getJobHistories().isEmpty()) {
            List<EmployeeJobHistory> histories = request.getJobHistories().stream().map(j -> {
                EmployeeJobHistory job = new EmployeeJobHistory();
                job.setEmployeeProfile(employeeProfile);

                if (j.getDepartmentId() != null) {
                    Department jobDept = departmentRepository.findById(j.getDepartmentId())
                            .orElseThrow(() -> new RuntimeException("Department not found for job history"));
                    job.setDepartment(jobDept);
                }

                job.setPosition(j.getPosition());
                job.setContractType(j.getContractType());
                job.setStartDate(j.getStartDate());
                job.setEndDate(j.getEndDate());
                job.setEmploymentStatus(j.getEmploymentStatus());
                job.setNote(j.getNote());
                return job;
            }).toList();

            employeeProfile.setJobHistories(histories);
        }

        // ====== Competencies ======
        if (request.getCompetencies() != null && !request.getCompetencies().isEmpty()) {
            List<EmployeeCompetency> competencies = request.getCompetencies().stream().map(c -> {
                EmployeeCompetency competency = new EmployeeCompetency();
                competency.setEmployeeProfile(employeeProfile);
                competency.setType(c.getType());
                competency.setName(c.getName());
                competency.setLevel(c.getLevel());
                competency.setIssuedBy(c.getIssuedBy());
                competency.setIssuedDate(c.getIssuedDate());
                competency.setNote(c.getNote());
                return competency;
            }).toList();

            employeeProfile.setCompetencies(competencies);
        }

        // ====== Lưu EmployeeProfile ======
        employeeProfileRepository.save(employeeProfile);


        return true;
    }

//    public List<EmployeeProfileResponse> getAllEmployeeProfiles() {
//        return employeeProfileRepository.findAll().stream()
//                .map(profile -> {
//                    String avatarUrl = null;
//                    if (profile.getAvatar() != null) {
//                        avatarUrl = fileStorageService.getPresignedUrl("employee-avatars", profile.getAvatar());
//                    }
//                    return EmployeeProfileResponse.toResponse(profile, avatarUrl);
//                })
//                .toList();
//    }
    public Page<EmployeeProfileResponse> getAllEmployeeProfiles(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        Page<EmployeeProfile> pageResult;

        if (keyword != null && !keyword.isEmpty()) {
            pageResult = employeeProfileRepository.findByFullNameContainingIgnoreCase(keyword, pageable);
        } else {
            pageResult = employeeProfileRepository.findAll(pageable);
        }

        // ✅ Map entity → DTO kèm avatar presigned URL
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
                        job.getDepartment() != null ? job.getDepartment().getName() : null,
                        job.getPosition(),
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
}
