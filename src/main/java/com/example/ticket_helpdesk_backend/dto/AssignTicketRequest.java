package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTicketRequest {
    UUID ticketId;
    UUID assigneeId;
}
