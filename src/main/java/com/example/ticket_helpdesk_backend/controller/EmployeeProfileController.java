package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.BucketName;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmployeeDocumentService;
import com.example.ticket_helpdesk_backend.service.EmployeeProfileService;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeProfileController {
    @Autowired
    private EmployeeProfileService employeeProfileService;

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private EmployeeDocumentService employeeDocumentService;

    private final String bucketName = "employee-avatars";

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEmployeeProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("profile") CreateEmployeeProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) throws ResourceNotFoundException, IOException {
        String token = authHeader.substring(7);
        String fileUrl;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            fileUrl = fileStorageService.uploadFile(BucketName.EMPLOYEE_AVATAR.getBucketName(), avatarFile, request.getFullName());
        } else {
            ClassPathResource defaultAvatar = new ClassPathResource("static/avatars/default.png");
            try (InputStream inputStream = defaultAvatar.getInputStream()) {
                fileUrl = fileStorageService.uploadFile(BucketName.EMPLOYEE_AVATAR.getBucketName(), inputStream, "default.png", request.getFullName());
            }
        }

        request.setAvatar(fileUrl); // gắn link ảnh vào DTO

        boolean success = employeeProfileService.createEmployeeProfile(request, token);
        String message = success ? "Create Employee Successfully" : "Create Employee Failed";


        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }


    // 1. Lấy tất cả nhân viên
    @GetMapping
    public ResponseEntity<?> getAllEmployee(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<EmployeeProfileResponse> employeePage = employeeProfileService.getAllEmployeeProfiles(page, size, keyword);

        Map<String, Object> meta = Map.of(
                "page", employeePage.getNumber(),
                "size", employeePage.getSize(),
                "totalPages", employeePage.getTotalPages(),
                "totalElements", employeePage.getTotalElements()
        );

        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get all employees successfully",
                LocalDateTime.now(),
                employeePage.getContent(),
                meta
        );

        return ResponseEntity.ok(response);
    }


    // 2. Lấy tất cả nhân viên của 1 phòng ban
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getAllEmployeeOfDepartment(@PathVariable UUID departmentId) {
        var employees = employeeProfileService.getEmployeesByDepartment(departmentId);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get employees by department successfully",
                LocalDateTime.now(),
                employees
        );
        return ResponseEntity.ok(response);
    }

    // 3. Lấy chi tiết 1 hồ sơ nhân viên
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeProfileById(@PathVariable UUID id) {
        var employee = employeeProfileService.getEmployeeProfileById(id);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get employee profile successfully",
                LocalDateTime.now(),
                employee
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatarEmployeeProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) throws ResourceNotFoundException, IOException {
        String token = authHeader.substring(7);

        boolean success = employeeProfileService.uploadAvatar(token, avatarFile);
        String message = success ? "Upload avatar successfully" : "Upload avatar Failed";

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }



//    @GetMapping("/me")
//    public ResponseEntity<?> getMyEmployeeProfile(@RequestHeader("Authorization") String authHeader) {
//        String token = authHeader.substring(7);
//        EmployeeProfile employeeProfile = employeeProfileService.getMyEmployeeProfile(token);
//        String avatar = fileStorageService.getPresignedUrl("employee-avatars", employeeProfile.getAvatar());
//        EmployeeProfileResponse profile = EmployeeProfileResponse.toResponse(employeeProfile, avatar);
//
//        ApiResponse<?> response = new ApiResponse<>(
//                HttpStatus.OK.value(),
//                "Get employee profile successfully",
//                LocalDateTime.now(),
//                profile
//        );
//        return ResponseEntity.ok(response);
//    }

    //@PreAuthorize("@securityService.hasRole('EMPLOYEE')")
    @PatchMapping("/me/contact-info")
    public ApiResponse<?> updateMyContactInfo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody EmployeeContactInfoUpdateDto request) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        EmployeeContactInfoUpdateDto updated = employeeProfileService.updateMyContactInfo(token, request);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Update contact info successfully",
                LocalDateTime.now(),
                updated
        );
    }

    // 1️⃣ Thông tin cơ bản
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        var profile = employeeProfileService.getBasicProfile(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Get basic employee profile successfully", LocalDateTime.now(), profile));
    }

    // 2️⃣ Lịch sử công việc
    @GetMapping("/me/jobs")
    public ResponseEntity<?> getMyJobHistories(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        var jobs = employeeProfileService.getMyJobHistories(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Get job history employee profile successfully", LocalDateTime.now(), jobs));
    }

    // 3️⃣ Năng lực
    @GetMapping("/me/competencies")
    public ResponseEntity<?> getMyCompetencies(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        var competencies = employeeProfileService.getMyCompetencies(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Get competencies employee profile successfully", LocalDateTime.now(), competencies));
    }

    // 4️⃣ Tài liệu
    @GetMapping("/me/documents")
    public ResponseEntity<?> getMyDocuments(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        var documents = employeeProfileService.getMyDocuments(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Get document employee profile successfully", LocalDateTime.now(), documents));
    }

}
