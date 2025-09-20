package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.EmailRequest;
import com.example.ticket_helpdesk_backend.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-simple-email")
    public ApiResponse<String> sendMail(@RequestBody EmailRequest request) {
        try {
            emailService.sendSimpleMail(request.getTo(), request.getSubject(), request.getText());

            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Email sent successfully",
                    LocalDateTime.now(),
                    "Sent to: " + request.getTo()
            );
        } catch (Exception e) {
            return new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to send email: " + e.getMessage(),
                    LocalDateTime.now(),
                    null
            );
        }
    }
}
