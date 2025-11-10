package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.AbsenceBalance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link AbsenceBalance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceBalanceResponse implements Serializable {
    UUID id;
    @NotNull
    UserDto user;
    BasicAbsenceTypeResponse absenceType;
    @NotNull
    Integer year;
    @NotNull
    BigDecimal totalDays;
    @NotNull
    BigDecimal usedDays;
    BigDecimal carriedOver;
    LocalDateTime lastUpdated;
}