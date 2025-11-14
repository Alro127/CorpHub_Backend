package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeShiftDto {
    private UUID id;
    private LocalDate workDate;
    private UUID shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
    private WorkScheduleStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}
