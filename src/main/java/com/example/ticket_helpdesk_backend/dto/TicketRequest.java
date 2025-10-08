package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Ticket}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest implements Serializable {
    UUID id;
    UUID categoryId;
    UUID assigneeId;
    UUID departmentId;
    @Size(max = 255)
    String title;
    String description;
    String priority;
}