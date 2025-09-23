package com.example.ticket_helpdesk_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeetingResponse {
    private String id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerEmail;
    private List<String> attendeesEmails;
}

