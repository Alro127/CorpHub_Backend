package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkflowStepType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class WorkflowStepResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 200)
    String name;
    @NotNull
    Integer stepOrder;
    @NotNull
    WorkflowStepType stepType;
    @Size(max = 100)
    String assignedRole;
    String conditionExpr;
    @NotNull
    LocalDateTime createdAt;
}