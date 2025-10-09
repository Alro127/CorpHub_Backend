package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.TicketPriority;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Ticket}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest implements Serializable {

    private UUID id;

    @NotNull(message = "Category ID cannot be null")
    private UUID categoryId;

    private UUID assigneeId;

    @NotNull(message = "Department ID cannot be null")
    private UUID departmentId;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Priority cannot be null")
    private TicketPriority priority;
}
