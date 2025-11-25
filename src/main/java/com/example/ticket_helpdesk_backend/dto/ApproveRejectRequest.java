package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRejectRequest {
    private boolean approve;
    private String comment;
}
