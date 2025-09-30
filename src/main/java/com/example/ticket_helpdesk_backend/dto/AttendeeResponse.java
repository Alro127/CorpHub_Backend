package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.AttendeeStatus;
import lombok.Data;

@Data
public class AttendeeResponse {
    private String email;
    private AttendeeStatus status;
}
