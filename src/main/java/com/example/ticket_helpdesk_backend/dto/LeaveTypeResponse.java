package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.LeaveType}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveTypeResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 50)
    String code;
    @NotNull
    @Size(max = 100)
    String name;
    String description;
    Boolean requireProof;
    Integer requireApprovalLv;
    Boolean affectQuota;
    BigDecimal maxPerRequest;
    Gender genderLimit;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}