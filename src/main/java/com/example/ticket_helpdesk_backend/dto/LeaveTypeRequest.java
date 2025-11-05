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
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.LeaveType}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveTypeRequest implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 50)
    String code;
    @NotNull(message = "Name must not be null")
    @Size(max = 100, message = "Name's length must be not longer than 100")
    String name;
    @Size(max = 1000, message = "Description's length must be not longer than 100")
    String description;
    Boolean requireProof;
    Integer requireApprovalLv;
    Boolean affectQuota;
    BigDecimal maxPerRequest;
    @NotNull(message = "Gender must not be null")
    Gender genderLimit;
}