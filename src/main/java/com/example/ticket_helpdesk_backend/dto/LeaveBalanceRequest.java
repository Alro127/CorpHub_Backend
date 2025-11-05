package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.LeaveBalance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceRequest implements Serializable {
    UUID id;
    UUID userId;
    UUID leaveTypeId;
    @NotNull
    Integer year;
    @NotNull
    BigDecimal totalDays;
    @NotNull
    BigDecimal usedDays;
    BigDecimal carriedOver;
}