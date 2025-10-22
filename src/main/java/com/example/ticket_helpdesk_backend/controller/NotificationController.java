package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.NotificationDto;
import com.example.ticket_helpdesk_backend.entity.Notification;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.NotificationService;
import com.example.ticket_helpdesk_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> create( @RequestBody NotificationDto request) {
        ApiResponse<NotificationDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
         "Create notification successfully",
                LocalDateTime.now(),
                service.sendNotification(request),
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?>  getUserNotifs(@RequestHeader("Authorization") String authHeader) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        User user = userService.getUserFromToken(token);
        ApiResponse<List<NotificationDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Create notification successfully",
                LocalDateTime.now(),
                service.getNotifications(user.getId()),
                null
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable UUID id) {
        service.markAsRead(id);
    }
}

