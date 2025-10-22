package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmailService;
import com.example.ticket_helpdesk_backend.service.EmployeeProfileService;
import com.example.ticket_helpdesk_backend.service.TicketService;
import com.example.ticket_helpdesk_backend.service.UserService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Autowired
    EmployeeProfileService employeeProfileService;

    @Autowired
    TicketService ticketService;

    @Autowired
    JwtUtil jwtUtil;

//    @PreAuthorize("@securityService.hasRole('ADMIN')")
//    @GetMapping("/get-all")
//    public ResponseEntity<?> getAllUser() {
//        List<UserDto> userDtoList = userService.getAllUser();
//        ApiResponse<List<UserDto>> apiResponse = new ApiResponse<>(
//                HttpStatus.OK.value(),
//                "All user found",
//                LocalDateTime.now(),
//                userDtoList
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/employee")
    public ResponseEntity<?> getEmployees(        @RequestHeader("Authorization") String authHeader,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String gender,
                                                  @RequestParam(required = false) String departmentId,
                                                  @RequestParam(required = false) Boolean isActive,
                                                  @RequestParam(defaultValue = "fullName") String sortField,
                                                  @RequestParam(defaultValue = "asc") String sortDir)throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        Page<UserDto> userDtoList = userService.getEmployees(token, page, size, keyword, gender, departmentId, isActive, sortField, sortDir);
        ApiResponse<List<UserDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All user found",
                LocalDateTime.now(),
                userDtoList.getContent(),
                Map.of(
                        "page", userDtoList.getNumber(),
                        "size", userDtoList.getSize(),
                        "totalElements", userDtoList.getTotalElements(),
                        "totalPages", userDtoList.getTotalPages(),
                        "last", userDtoList.isLast()
                )

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
    public ResponseEntity<?> getUserInfo() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = userService.getUserDtoByUsername(authentication.getPrincipal().toString());
        ApiResponse<UserDto> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User info",
                LocalDateTime.now(),
                userDto
        );

        return ResponseEntity.ok(apiResponse);
    }

    //@PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')" )
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestBody CreateUserRequest request,
            @RequestParam(required = false) UUID ticketId,
            @RequestHeader("Authorization") String authHeader) throws Exception {

        System.out.println("Create User request: " + request);
        boolean success = userService.createUser(request);

        if (success) {
            EmployeeProfile employeeProfile = employeeProfileService.getEmployeeProfileById(request.getEmployeeId());
            emailService.sendSimpleMail(
                    employeeProfile.getPersonalEmail(),
                    "Tài khoản nhân viên mới",
                    "Email công ty: " + request.getEmail() + " Mật khẩu đăng nhập: " + request.getPassword()
            );

            if (ticketId != null) {
                String token = authHeader.substring(7);
                User user = userService.getUserFromToken(token);
                ticketService.complete(ticketId, user.getId());
            }
        }

        String message = success ? "Create User Successfully" : "Create User Failed";
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

        //Chưa sửa xong;
        //UserDataResponse user = userService.getEmployeeById(userId);
        UserDataResponse user = new UserDataResponse();

        ApiResponse<UserDataResponse> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User found",
                LocalDateTime.now(),
                user
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/search")
    public ResponseEntity<?> getUsersBySearch(@RequestParam("keyword") String keyword) {
        ApiResponse<List<UserDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "User list",
                LocalDateTime.now(),
                userService.getUsersBySearch(keyword)
        );

        return ResponseEntity.ok(apiResponse);
    }
}
