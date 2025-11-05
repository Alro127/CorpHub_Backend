package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTimelineResponse {
    private LocalDateTime time;
    private String text;
}
