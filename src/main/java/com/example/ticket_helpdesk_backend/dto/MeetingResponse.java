package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MeetingResponse {
    private UUID id;
    private String subject;
    private String title;
    private String description;
    private String onlineLink;
    private String location;
    private Boolean meetingRoom;
    private RoomRequirementDto roomRequirement;
    private Boolean ready;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerEmail;
    private List<AttendeeResponse> attendees;
}

