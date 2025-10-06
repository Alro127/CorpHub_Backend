package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class MeetingRoomRequest implements Serializable {
    UUID id;
    UUID meetingId;
    UUID roomId;
    @NotNull
    LocalDateTime startTime;
    @NotNull
    LocalDateTime endTime;
    @Size(max = 255)
    String note;
}