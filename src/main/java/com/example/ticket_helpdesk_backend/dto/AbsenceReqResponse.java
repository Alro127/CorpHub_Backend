package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.AbsenceRequestStatus;
import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link AbsenceRequest}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceReqResponse implements Serializable {
    UUID id;
    @NotNull
    UserDto user;
    BasicAbsenceTypeResponse absenceType;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    @NotNull
    BigDecimal durationDays;
    String reason;
    @Size(max = 255)
    String attachmentUrl;
    @NotNull
    @Size(max = 20)
    AbsenceRequestStatus status;
    UserDto approver;
    LocalDateTime approvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}