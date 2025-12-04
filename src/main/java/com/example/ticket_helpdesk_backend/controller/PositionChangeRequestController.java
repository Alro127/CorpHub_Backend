package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestCreateDto;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestDetailDto;
import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import com.example.ticket_helpdesk_backend.service.PositionChangeRequestService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/position-change-request")
@RequiredArgsConstructor
public class PositionChangeRequestController {

    private final PositionChangeRequestService service;


    @PostMapping
    public ResponseEntity<?> create(@RequestBody PositionChangeRequestCreateDto req, @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        ApiResponse<PositionChangeRequestDetailDto> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Create PositionChangeRequestDetailDto successfully",
                LocalDateTime.now(),
                service.createRequest(req, token)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id) {
        ApiResponse<PositionChangeRequestDetailDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get PositionChangeRequestDetailDto successfully",
                LocalDateTime.now(),
                service.getRequest(id)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> list(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.getRequestsByEmployee(employeeId));
    }
}
