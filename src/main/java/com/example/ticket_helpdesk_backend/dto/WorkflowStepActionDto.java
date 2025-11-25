package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.WorkflowActionType;
import com.example.ticket_helpdesk_backend.entity.WorkflowStepAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStepActionDto {

    private UUID id;

    private UUID actorId;
    private String actorName;

    private UUID stepId;
    private String stepName;
    private Integer stepOrder;

    private WorkflowActionType action;
    private String comment;

    private LocalDateTime createdAt;

    static public WorkflowStepActionDto toDto(WorkflowStepAction action) {

        return WorkflowStepActionDto.builder()
                .id(action.getId())
                .actorId(action.getActor().getId())
                .actorName(action.getActor().getEmployeeProfile().getFullName())
                .stepId(action.getStep().getId())
                .stepName(action.getStep().getName())
                .stepOrder(action.getStep().getStepOrder())
                .action(action.getAction())
                .comment(action.getComment())
                .createdAt(action.getCreatedAt())
                .build();
    }

    static public List<WorkflowStepActionDto> toDtoList(List<WorkflowStepAction> actions) {
        return actions.stream()
                .map(WorkflowStepActionDto::toDto)
                .toList();
    }
}
