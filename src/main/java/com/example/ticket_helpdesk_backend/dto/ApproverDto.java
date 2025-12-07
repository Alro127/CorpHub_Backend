package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApproverDto {
    private UUID id;
    private String fullName;
    private String positionName;
    private String departmentName;
}
