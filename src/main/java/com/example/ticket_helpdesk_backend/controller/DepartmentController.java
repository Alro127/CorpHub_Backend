package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.DepartmentDto;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {
    @Autowired
    private final DepartmentService departmentService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllDepartments() {
        List<DepartmentDto> departmentDtoList = departmentService.getDepartmentDtoList();
        ApiResponse<List<DepartmentDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All departments found",
                LocalDateTime.now(),
                departmentDtoList);

        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/users")
    public ResponseEntity<?> getUsersDepartment(@RequestHeader("Authorization") String authHeader) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        List<UserDto> usersDepartmentList = departmentService.getUsersByDepartment(token);
        ApiResponse<List<UserDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All users of Department found",
                LocalDateTime.now(),
                usersDepartmentList
        );
        return ResponseEntity.ok(response);
    }


}
