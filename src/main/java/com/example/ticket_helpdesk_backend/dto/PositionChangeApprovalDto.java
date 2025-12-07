package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PositionChangeApprovalDto {

    private UUID id;

    private Integer stepOrder;  // Step 1,2,3...
    private String role;        // MANAGER/HR/ADMIN
    private String decision;    // PENDING/APPROVED/REJECTED

    private String comment;
    private LocalDateTime decidedAt;

    private ApproverDto approver; // thông tin người duyệt

    public static PositionChangeApprovalDto mapToDto(PositionChangeApproval entity) {
        PositionChangeApprovalDto dto = new PositionChangeApprovalDto();

        dto.setId(entity.getId());
        dto.setStepOrder(entity.getStepOrder());
        dto.setRole(entity.getRole());
        dto.setDecision(entity.getDecision());
        dto.setComment(entity.getComment());
        dto.setDecidedAt(entity.getDecidedAt());

        ApproverDto approverDto = new ApproverDto();
        approverDto.setId(entity.getApprover().getId());
        approverDto.setFullName(entity.getApprover().getFullName());

        if (entity.getApprover().getPosition() != null)
            approverDto.setPositionName(entity.getApprover().getPosition().getName());

        if (entity.getApprover().getDepartment() != null)
            approverDto.setDepartmentName(entity.getApprover().getDepartment().getName());

        dto.setApprover(approverDto);

        return dto;
    }

}

