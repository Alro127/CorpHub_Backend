package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.CreateEmployeeProfileRequest;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.service.EmployeeProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/employee")
public class EmployeeProfileController {
    @Autowired
    EmployeeProfileService employeeProfileService;

    @PostMapping("/create")
    public ResponseEntity<?> createEmployeeProfile(@RequestBody CreateEmployeeProfileRequest request) {

        System.out.println("Create User request: " + request);
        boolean success = employeeProfileService.CreateEmployeeProfile(request);
        String message = success ? "Create User Successfully" : "Create User Failed";
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
}
