package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Ticket}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse implements Serializable {
    UUID id;
    TicketCategoryDto category;
    NameInfoDto requester;
    NameInfoDto assignee;
    DepartmentDto department;
    @Size(max = 255)
    String title;
    String description;
    String priority;
    String status;
    LocalDateTime assignedAt;
    LocalDateTime resolvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}