package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.DepartmentDto;
import com.example.ticket_helpdesk_backend.service.DepartmentService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    @Autowired
    private final DepartmentService departmentService;
    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        List<DepartmentDto> departmentDtoList = departmentService.getDepartmentDtoList();
        ApiResponse<List<DepartmentDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All departments found",
                LocalDateTime.now(),
                departmentDtoList);

        return ResponseEntity.ok(apiResponse);
    }
}
