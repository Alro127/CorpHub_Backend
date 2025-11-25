package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeJobHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobHistoryResponse {
    private UUID id;
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String employmentStatus;
    private String note;

    public static EmployeeJobHistoryResponse fromEntity(EmployeeJobHistory entity) {
        if (entity == null) return null;
        return new EmployeeJobHistoryResponse(
                entity.getId(),
                entity.getContractType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getEmploymentStatus(),
                entity.getNote()
        );
    }
}