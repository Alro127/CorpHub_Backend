package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomAllocationSuggestion {
    private UUID requirementId;
    private UUID roomId;
    private String roomName;
    private double matchScore;
}
