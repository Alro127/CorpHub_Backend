package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import com.example.ticket_helpdesk_backend.service.PositionChangeApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/position-change-approval")
@RequiredArgsConstructor
public class PositionChangeApprovalController {

    private final PositionChangeApprovalService service;

    @PostMapping("/{id}/approve")
    public ResponseEntity<PositionChangeApproval> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(service.approve(id, body.get("comment")));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PositionChangeApproval> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(service.reject(id, body.get("comment")));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<PositionChangeApproval>> getSteps(@PathVariable UUID requestId) {
        return ResponseEntity.ok(service.getSteps(requestId));
    }
}

