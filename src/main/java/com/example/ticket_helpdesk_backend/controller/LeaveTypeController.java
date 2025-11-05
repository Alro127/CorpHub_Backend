package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.LeaveTypeRequest;
import com.example.ticket_helpdesk_backend.dto.LeaveTypeResponse;
import com.example.ticket_helpdesk_backend.entity.LeaveType;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.LeaveTypeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave/type")
@AllArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<?> getAllLeaveTypes() {
        ApiResponse<List<LeaveTypeResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched leave types successfully",
                LocalDateTime.now(),
                leaveTypeService.getAll()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) throws ResourceNotFoundException {
        LeaveTypeResponse leaveType = leaveTypeService.getById(id);
        ApiResponse<LeaveTypeResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched leave type successfully",
                LocalDateTime.now(),
                leaveType
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createLeaveType(@RequestBody LeaveTypeRequest request) {
        LeaveTypeResponse created = leaveTypeService.create(request);
        ApiResponse<LeaveTypeResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Created leave type successfully",
                LocalDateTime.now(),
                created
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeaveType(@PathVariable UUID id,
                                             @RequestBody LeaveTypeRequest request) throws ResourceNotFoundException {
        LeaveTypeResponse updated = leaveTypeService.update(id, request);
        ApiResponse<LeaveTypeResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated leave type successfully",
                LocalDateTime.now(),
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeaveType(@PathVariable UUID id) throws ResourceNotFoundException {
        leaveTypeService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Deleted leave type successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
