package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompetencyResponse {
    private UUID id;
    private String type;   // SKILL, DEGREE, CERTIFICATION, LANGUAGE
    private String name;
    private String level;
    private String issuedBy;
    private LocalDate issuedDate;
    private String note;

    public static EmployeeCompetencyResponse fromEntity(EmployeeCompetency entity) {
        if (entity == null) return null;
        return new EmployeeCompetencyResponse(
                entity.getId(),
                entity.getType(),
                entity.getName(),
                entity.getLevel(),
                entity.getIssuedBy(),
                entity.getIssuedDate(),
                entity.getNote()
        );
    }
}