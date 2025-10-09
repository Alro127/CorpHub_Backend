package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
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
public class TicketMetaResponse implements Serializable {
    UUID id;
    String meta;

    static public TicketMetaResponse toResponse(Ticket ticket) {
        TicketMetaResponse dto = new TicketMetaResponse();
        dto.setId(ticket.getId());
        dto.setMeta(ticket.getMeta());




        return dto;
    }
}