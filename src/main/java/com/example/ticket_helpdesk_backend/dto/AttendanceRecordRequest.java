package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.AttendanceRecord}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordRequest implements Serializable {
    Double lat;
    Double lng;
    @Size(max = 50)
    String ip;
}