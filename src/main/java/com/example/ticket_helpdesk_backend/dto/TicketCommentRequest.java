package com.example.ticket_helpdesk_backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TicketCommentRequest {
    UUID id;
    UUID ticketId;
    UUID parentId;
    String commentText;
}
