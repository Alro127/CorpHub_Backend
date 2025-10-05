package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.service.RoomRequirementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/room-requirements")
public class RoomRequirementController {
    private final RoomRequirementService roomRequirementService;

    public RoomRequirementController(RoomRequirementService roomRequirementService) {
        this.roomRequirementService = roomRequirementService;
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getRoomRequirements() {
        ApiResponse<List<RoomRequirementDto>> response = new ApiResponse<> (
            HttpStatus.OK.value(),
            "Fetch room requirements successfully",
            LocalDateTime.now(),
            roomRequirementService.getAllRoomRequirements()
        );
        return ResponseEntity.ok(response);
    }
}
