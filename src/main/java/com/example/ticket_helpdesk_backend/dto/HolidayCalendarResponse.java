package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.HolidayCalendar}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayCalendarResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @NotNull
    LocalDate date;
    String description;
    Boolean isRecurring;
    LocalDateTime createdAt;
}