package com.example.ticket_helpdesk_backend.dto;


import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalWorkHistoryDto {

    private UUID id;
    private UUID employeeId;

    private String departmentName;
    private String positionName;

    private LocalDate effectiveDate;

    private String reason;
    private String changeType;

    private UUID requestId;       // nullable nếu không có request

    public static InternalWorkHistoryDto mapToDto(InternalWorkHistory entity) {
        return new InternalWorkHistoryDto(
                entity.getId(),
                entity.getEmployeeProfile().getId(),
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.getPosition() != null ? entity.getPosition().getName() : null,
                entity.getEffectiveDate(),
                entity.getReason(),
                entity.getChangeType(),
                entity.getRequest().getId()
        );
    }
}
