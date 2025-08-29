package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Ticket}
 */
@Value
public class TicketRequest implements Serializable {
    UUID id;
    UUID categoryId;
    UUID requesterId;
    UUID assigneeId;
    UUID departmentId;
    @Size(max = 255)
    String title;
    String description;
    @Size(max = 50)
    String priority;
}