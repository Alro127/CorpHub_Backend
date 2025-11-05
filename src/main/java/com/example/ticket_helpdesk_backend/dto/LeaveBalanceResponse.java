package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.LeaveBalance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceResponse implements Serializable {
    UUID id;
    @NotNull
    UserDto user;
    String leaveTypeCode;
    String leaveTypeName;
    @NotNull
    Integer year;
    @NotNull
    BigDecimal totalDays;
    @NotNull
    BigDecimal usedDays;
    BigDecimal carriedOver;
    LocalDateTime lastUpdated;
}