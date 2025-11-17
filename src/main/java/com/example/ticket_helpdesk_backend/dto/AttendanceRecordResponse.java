package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.AttendanceRecord}
 */
@Data
@RequiredArgsConstructor
public class AttendanceRecordResponse implements Serializable {
    UUID id;
    @NotNull
    WorkScheduleResponse workSchedule;
    LocalDateTime checkInTime;
    LocalDateTime checkOutTime;
    Double checkInLat;
    Double checkInLng;
    Double checkOutLat;
    Double checkOutLng;
    @Size(max = 50)
    String checkInIp;
    @Size(max = 50)
    String checkOutIp;
    @NotNull
    LocalDateTime createdAt;
    @NotNull
    LocalDateTime updatedAt;
}