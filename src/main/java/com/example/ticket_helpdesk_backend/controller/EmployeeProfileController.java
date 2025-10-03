package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.CreateEmployeeProfileRequest;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.dto.EmployeeProfileResponse;
import com.example.ticket_helpdesk_backend.service.EmployeeProfileService;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
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
            @RequestPart("profile") CreateEmployeeProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileUrl = fileStorageService.uploadFile(bucketName, avatarFile, request.getFullName());
            request.setAvatar(fileUrl); // gắn link ảnh vào DTO
        }

        boolean success = employeeProfileService.createEmployeeProfile(request);
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
    public ResponseEntity<?> getAllEmployee() {
        List<EmployeeProfileResponse> employees = employeeProfileService.getAllEmployeeProfiles();
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get all employees successfully",
                LocalDateTime.now(),
                employees
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
}
