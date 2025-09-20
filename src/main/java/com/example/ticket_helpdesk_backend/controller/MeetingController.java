package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.service.EmailService;
import com.example.ticket_helpdesk_backend.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final EmailService emailService;
    private final MeetingService meetingService;

    public MeetingController(EmailService emailService, MeetingService meetingService) {
        this.emailService = emailService;
        this.meetingService = meetingService;
    }

    // Tạo + Lưu DB + Gửi invite (.ics)
    @PostMapping("/invite")
    public ApiResponse<String> sendInvite(@RequestBody MeetingRequest request) {
        try {
            Meeting saved = meetingService.createMeeting(request);
            emailService.sendMeetingInvite(request);

            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Meeting invite sent & saved successfully",
                    LocalDateTime.now(),
                    "meeting_id=" + saved.getId()
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to send invite: " + e.getMessage(),
                    LocalDateTime.now(),
                    null
            );
        }
    }

    @GetMapping
    public ApiResponse<List<Meeting>> getAllMeetings() {
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched meetings successfully",
                LocalDateTime.now(),
                meetingService.getAllMeetings()
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
