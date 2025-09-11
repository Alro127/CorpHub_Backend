package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

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
}
