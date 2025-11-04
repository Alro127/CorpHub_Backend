package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.RoomTypeDto;
import com.example.ticket_helpdesk_backend.entity.RoomType;
import com.example.ticket_helpdesk_backend.service.RoomTypeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@AllArgsConstructor
public class RoomTypeController {
    private RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<?> getAllRoomTypes() {
        ApiResponse<List<RoomTypeDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Room types fetched successfully",
                LocalDateTime.now(),
                roomTypeService.getAllRoomTypes()
        );

        return ResponseEntity.ok(response);
    }
}
