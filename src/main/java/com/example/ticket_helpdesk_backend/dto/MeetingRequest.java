package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.validation.ValidTimeRange;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@ValidTimeRange
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRequest {

    private UUID id;

    @NotEmpty(message = "Recipient list cannot be empty")
    @Size(max = 50, message = "Recipient list cannot exceed 50 emails")
    private List<@Email(message = "Each recipient must be a valid email") String> to;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 200, message = "Subject cannot exceed 200 characters")
    private String subject;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private boolean meetingRoom; // optional — defaults to false

    @Valid
    private RoomRequirementDto roomRequirement;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    private String onlineLink;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime end;
}
