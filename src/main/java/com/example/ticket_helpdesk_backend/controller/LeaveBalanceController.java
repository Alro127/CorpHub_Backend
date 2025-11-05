package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.LeaveBalanceRequest;
import com.example.ticket_helpdesk_backend.dto.LeaveBalanceResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.LeaveBalanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/leave/balance")
public class LeaveBalanceController {
    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    public ResponseEntity<?> getAllLeaveBalances() {
        ApiResponse<List<LeaveBalanceResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched leave balances successfully",
                LocalDateTime.now(),
                leaveBalanceService.getAll()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) throws ResourceNotFoundException {
        LeaveBalanceResponse LeaveBalance = leaveBalanceService.getById(id);
        ApiResponse<LeaveBalanceResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched leave balance successfully",
                LocalDateTime.now(),
                LeaveBalance
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createLeaveBalance(@RequestBody LeaveBalanceRequest request) throws ResourceNotFoundException {
        LeaveBalanceResponse created = leaveBalanceService.create(request);
        ApiResponse<LeaveBalanceResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Created leave balance successfully",
                LocalDateTime.now(),
                created
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLeaveBalance(@PathVariable UUID id,
                                             @RequestBody LeaveBalanceRequest request) throws ResourceNotFoundException {
        LeaveBalanceResponse updated = leaveBalanceService.update(id, request);
        ApiResponse<LeaveBalanceResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated leave balance successfully",
                LocalDateTime.now(),
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeaveBalance(@PathVariable UUID id) throws ResourceNotFoundException {
        leaveBalanceService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Deleted leave balance successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
