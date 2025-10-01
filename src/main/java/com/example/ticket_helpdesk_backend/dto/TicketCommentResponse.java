package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.TicketComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketCommentResponse {
    UUID id;
    UUID ticketId;
    UUID parentId;
    NameInfoDto author;
    String commentText;
    LocalDateTime updatedAt;

    static public TicketCommentResponse toResponse(TicketComment comment) {
        TicketCommentResponse response = new TicketCommentResponse();
        response.id = comment.getId();
        response.ticketId = comment.getTicket().getId();
        response.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        response.author = new NameInfoDto(comment.getUser().getId(), comment.getUser().getEmployeeProfile().getFullName(), comment.getUser().getEmployeeProfile().getAvatar());
        response.commentText = comment.getCommentText();
        response.updatedAt = comment.getUpdatedAt();
        return response;
    }
}
