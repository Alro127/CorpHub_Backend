package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.dto.RoomResponse;
import com.example.ticket_helpdesk_backend.service.RoomRequirementService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/room-requirements")
public class RoomRequirementController {
    private final RoomRequirementService roomRequirementService;

    public RoomRequirementController(RoomRequirementService roomRequirementService) {
        this.roomRequirementService = roomRequirementService;
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getRoomRequirements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RoomRequirementDto> pageData = roomRequirementService.getAllRoomRequirements(page, size);
        ApiResponse<List<RoomRequirementDto>> response = new ApiResponse<> (
            HttpStatus.OK.value(),
            "Fetch room requirements successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PutMapping("/approve")
    public ResponseEntity<?> approveRoomRequirement(
            @RequestParam(required = true) UUID id,
            @RequestParam(required = true) UUID roomId) {
        Boolean success = roomRequirementService.setUpRoom(id, roomId);
        ApiResponse<Boolean> response = new ApiResponse<>(
            HttpStatus.OK.value(),
                "Set room for room requirement",
                LocalDateTime.now(),
                success
        );

        return ResponseEntity.ok(response);
    }
}
