package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.AbsenceBalance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link AbsenceBalance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceBalanceRequest implements Serializable {
    UUID id;
    UUID userId;
    UUID absenceTypeId;
    @NotNull
    Integer year;
    @NotNull
    BigDecimal totalDays;
    @NotNull
    BigDecimal usedDays;
    BigDecimal carriedOver;
}