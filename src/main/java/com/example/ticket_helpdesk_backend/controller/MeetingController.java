package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    private final EmailService emailService;

    public MeetingController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/invite")
    public ApiResponse<String> sendInvite(@RequestBody MeetingRequest request) {
        try {
            System.out.println("debug");
            emailService.sendMeetingInvite(request);
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Meeting invite sent successfully",
                    LocalDateTime.now(),
                    "Sent to: " + String.join(", ", request.getTo())
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
}
