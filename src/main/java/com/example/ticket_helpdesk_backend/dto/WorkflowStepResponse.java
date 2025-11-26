package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkflowStepType;
import com.example.ticket_helpdesk_backend.model.ApproverDefinition;
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

    private UUID id;

    @NotNull
    @Size(max = 200)
    private String name;

    @NotNull
    private Integer stepOrder;

    @NotNull
    private WorkflowStepType stepType;

    /** Trả ra mô hình approver dạng JSON (type + params) */
    @NotNull
    private ApproverDefinition approver;

    /** Optional: điều kiện của step */
    private String conditionExpr;

    @NotNull
    private LocalDateTime createdAt;
}