package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Shift}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @NotNull
    LocalTime startTime;
    @NotNull
    LocalTime endTime;
    Boolean isNightShift;
    Integer flexibleMinutes;
    BigDecimal workingHours;
}