package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllUser() {
        List<UserDto> userDtoList = userService.getAllUser();
        ApiResponse<List<UserDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All user found",
                LocalDateTime.now(),
                userDtoList
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/employee")
    public ResponseEntity<?> getEmployees(@RequestHeader("Authorization") String authHeader) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        List<UserDto> userDtoList = userService.getEmployees(token);
        ApiResponse<List<UserDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All user found",
                LocalDateTime.now(),
                userDtoList
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/name-info")
    public ResponseEntity<?> getUser() {
        List<NameInfoDto> nameInfoDtoList = userService.getNameInfo();
        ApiResponse<List<NameInfoDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All user found",
                LocalDateTime.now(),
                nameInfoDtoList
        );

        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/my-info")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = userService.getUserDtoByEmail(authentication.getPrincipal().toString());
        ApiResponse<UserDto> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User info",
                LocalDateTime.now(),
                userDto
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')" )
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {

        System.out.println("Create User request: " + request);
        boolean success = userService.createUser(request);
        String message = success ? "Register Successfully" : "Register Failed";
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                message,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/employee/info/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable("id") String id) throws ResourceNotFoundException {
        UUID userId = UUID.fromString(id);

        UserDataResponse user = userService.getEmployeeById(userId);

        ApiResponse<UserDataResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User found",
                LocalDateTime.now(),
                user
        );

        return ResponseEntity.ok(apiResponse);
    }
}
