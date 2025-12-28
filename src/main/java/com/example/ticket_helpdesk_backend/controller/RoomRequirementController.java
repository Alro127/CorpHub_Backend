package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.RoomRequirementStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.RoomRequirementService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RoomRequirementStatus status) {
        Page<RoomRequirementDto> pageData = roomRequirementService.getAllRoomRequirements(page, size, status);
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
            @RequestParam UUID id,
            @RequestParam UUID roomId) {
        Boolean success = roomRequirementService.setUpRoom(id, roomId);
        ApiResponse<Boolean> response = new ApiResponse<>(
            HttpStatus.OK.value(),
                "Set room for room requirement",
                LocalDateTime.now(),
                success
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<?> getRoomRequirementsByRoomIdAndDate(
            @RequestParam UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws ResourceNotFoundException {
        List<RoomRequirementDto> roomRequirementDtoList = roomRequirementService.getRoomRequirementsByRoomAndDate(roomId, date);
        ApiResponse<List<RoomRequirementDto>> response = new ApiResponse<> (
                HttpStatus.OK.value(),
                roomRequirementDtoList.isEmpty() ? "Not found" :"List room requirements successfully",
                LocalDateTime.now(),
                roomRequirementDtoList
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping("/allocations/suggestions")
    public ResponseEntity<?> allocationSuggestions(@RequestBody ListUUIDRequest request) {
        List<UUID> ids = request.getIds();

        List<RoomAllocationSuggestion> suggestions = roomRequirementService.suggestAllocations(ids);

        ApiResponse<List<RoomAllocationSuggestion>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Room allocation suggestions generated successfully",
                LocalDateTime.now(),
                suggestions
        );

        return ResponseEntity.ok(response);
    }
}
