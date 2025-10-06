package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.MeetingRoom}
 */
@Value
public class MeetingRoomResponse implements Serializable {
    UUID id;
    @NotNull
    MeetingResponse meeting;
    @NotNull
    RoomResponse room;
    @NotNull
    LocalDateTime startTime;
    @NotNull
    LocalDateTime endTime;
    @Size(max = 255)
    String note;
}