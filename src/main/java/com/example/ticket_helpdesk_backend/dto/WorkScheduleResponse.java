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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.WorkSchedule}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkScheduleResponse implements Serializable {
    UUID id;
    @NotNull
    UserDto user;
    @NotNull
    ShiftDto shift;
    @NotNull
    LocalDate workDate;
    WorkScheduleStatus status;
    LocalDateTime checkInTime;
    LocalDateTime checkOutTime;
}