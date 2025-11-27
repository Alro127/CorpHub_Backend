package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.Gender;
import com.example.ticket_helpdesk_backend.entity.AbsenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link AbsenceType}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceTypeResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 50)
    String code;
    @NotNull
    @Size(max = 100)
    String name;
    String description;
    Boolean requireProof;
    Boolean affectQuota;
    BigDecimal maxPerRequest;
    Gender genderLimit;
    UUID workflowTemplateId;
    String workflowTemplateName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}