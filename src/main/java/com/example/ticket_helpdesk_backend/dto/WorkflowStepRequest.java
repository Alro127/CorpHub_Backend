package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkflowStepType;
import com.example.ticket_helpdesk_backend.model.ApproverDefinition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class WorkflowStepRequest implements Serializable {

    @NotNull
    private UUID templateId;

    @NotNull
    @Size(max = 200)
    private String name;

    @NotNull
    private Integer stepOrder;

    @NotNull
    private WorkflowStepType stepType;

    /** ApproverDefinition chứa: type + params (USER, POSITION, ROLE, DEPARTMENT...) */
    @NotNull
    private ApproverDefinition approver;

    /** Optional condition */
    private String conditionExpr;
}
