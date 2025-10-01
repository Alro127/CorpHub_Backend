package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.TicketCommentRequest;
import com.example.ticket_helpdesk_backend.dto.TicketCommentResponse;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.TicketCommentService;
import com.example.ticket_helpdesk_backend.service.UserService;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-comments")
public class TicketCommentController {
    @Autowired
    private TicketCommentService ticketCommentService;
    @Autowired
    private UserService userService;


    @GetMapping
    public ApiResponse<List<TicketCommentResponse>> getTicketCommentsByTicketId(@RequestParam("ticketId") UUID ticketId) throws ResourceNotFoundException {
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get comments successfully",
                LocalDateTime.now(),
                ticketCommentService.getCommentsByTicketId(ticketId)
        );
    }

    @PostMapping("/save")
    public ApiResponse<TicketCommentResponse> saveTicketComment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TicketCommentRequest ticketCommentRequest) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        User user = userService.getUserFromToken(token);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Save the comment successfully",
                LocalDateTime.now(),
                ticketCommentService.save(ticketCommentRequest, user)
        );
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<UUID> deleteTicketComment(@RequestHeader("Authorization") String authHeader, @PathVariable UUID id) throws ResourceNotFoundException, AuthException {
        String token = authHeader.substring(7);
        User user = userService.getUserFromToken(token);

        boolean ok = ticketCommentService.deleteTicketComment(id, user);
        return new ApiResponse<>(
                ok ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value(),
                ok ? "Comment deleted successfully" : "Comment not found",
                LocalDateTime.now(),
                ok ? id : null
        );
    }
}
