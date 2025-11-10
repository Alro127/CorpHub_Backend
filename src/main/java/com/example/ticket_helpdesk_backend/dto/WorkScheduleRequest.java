package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.WorkSchedule}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkScheduleRequest implements Serializable {
    UUID id;
    UUID userId;
    UUID shiftId;
    @NotNull
    LocalDate workDate;
    WorkScheduleStatus status;
}