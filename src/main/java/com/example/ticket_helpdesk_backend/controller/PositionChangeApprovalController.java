package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.PositionChangeApprovalDto;
import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import com.example.ticket_helpdesk_backend.service.PositionChangeApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/position-change-approval")
@RequiredArgsConstructor
public class PositionChangeApprovalController {

    private final PositionChangeApprovalService service;

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        PositionChangeApproval result = service.approve(id, body.get("comment"));

        ApiResponse<PositionChangeApproval> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Approval successfully processed",
                LocalDateTime.now(),
                result
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        PositionChangeApproval result = service.reject(id, body.get("comment"));

        ApiResponse<PositionChangeApproval> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Rejection successfully processed",
                LocalDateTime.now(),
                result
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<?> getSteps(@PathVariable UUID requestId) {
        List<PositionChangeApprovalDto> steps = service.getSteps(requestId);

        ApiResponse<List<PositionChangeApprovalDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get approval steps successfully",
                LocalDateTime.now(),
                steps
        );

        return ResponseEntity.ok(response);
    }
}
