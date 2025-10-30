package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.CreateEmployeeProfileRequest;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.dto.EmployeeProfileResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmployeeProfileService;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeProfileController {
    @Autowired
    private EmployeeProfileService employeeProfileService;

    @Autowired
    private FileStorageService fileStorageService;

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
            fileUrl = fileStorageService.uploadFile(bucketName, avatarFile, request.getFullName());
        } else {
            ClassPathResource defaultAvatar = new ClassPathResource("static/avatars/default.png");
            try (InputStream inputStream = defaultAvatar.getInputStream()) {
                fileUrl = fileStorageService.uploadFile(bucketName, inputStream, "default.png", request.getFullName());
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
}
