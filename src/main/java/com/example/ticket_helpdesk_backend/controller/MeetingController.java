package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.dto.MeetingResponse;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmailService;
import com.example.ticket_helpdesk_backend.service.MeetingService;
import com.example.ticket_helpdesk_backend.service.UserService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final EmailService emailService;
    private final MeetingService meetingService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public MeetingController(EmailService emailService, MeetingService meetingService, JwtUtil jwtUtil, UserService userService) {
        this.emailService = emailService;
        this.meetingService = meetingService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @PostMapping("/save")
    public ApiResponse<MeetingResponse> saveMeeting(@RequestHeader("Authorization") String authHeader, @RequestBody MeetingRequest request) throws Exception {
        String token = authHeader.substring(7);
        User user = userService.getUserFromToken(token);

        MeetingResponse saved = meetingService.saveMeeting(request, user.getEmail());

        // Tạm thời bỏ qua để tạo dữ liệu
        //emailService.sendMeetingInvite(request, user.getEmail());

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Meeting invite sent & saved successfully",
                LocalDateTime.now(),
                saved
        );
    }

    @GetMapping
    public ApiResponse<List<MeetingResponse>> getAllMeetings(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched meetings successfully",
                LocalDateTime.now(),
                meetingService.getMeetings(userId)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<Meeting> getMeeting(@PathVariable UUID id) {
        try {
            Meeting m = meetingService.getMeetingOrThrow(id);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Fetched meeting successfully",
                    LocalDateTime.now(),
                    m
            );
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    null
            );
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Meeting> updateMeeting(@PathVariable UUID id, @RequestBody MeetingRequest req) {
        try {
            Meeting updated = meetingService.updateMeeting(id, req);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Updated meeting successfully",
                    LocalDateTime.now(),
                    updated
            );
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    LocalDateTime.now(),
                    null
            );
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteMeeting(@PathVariable UUID id) {
        boolean ok = meetingService.deleteMeeting(id);
        return new ApiResponse<>(
                ok ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value(),
                ok ? "Meeting deleted successfully" : "Meeting not found",
                LocalDateTime.now(),
                ok ? "deleted_id=" + id : null
        );
    }
}
