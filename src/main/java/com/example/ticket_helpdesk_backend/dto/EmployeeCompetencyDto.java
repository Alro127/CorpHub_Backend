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
public class EmployeeCompetencyDto {
    private UUID id;
    private String type;   // SKILL, DEGREE, CERTIFICATION, LANGUAGE
    private String name;
    private String level;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expireDate;
    private String note;

    public static EmployeeCompetencyDto fromEntity(EmployeeCompetency entity) {
        if (entity == null) return null;
        return new EmployeeCompetencyDto(
                entity.getId(),
                entity.getType(),
                entity.getName(),
                entity.getLevel(),
                entity.getIssuedBy(),
                entity.getIssuedDate(),
                entity.getExpireDate(),
                entity.getNote()
        );
    }

    public static EmployeeCompetency toEntity(EmployeeCompetencyDto dto) {
        if (dto == null) return null;
        EmployeeCompetency entity = new EmployeeCompetency(

        );
        entity.setId(dto.getId());
        entity.setType(dto.getType());
        entity.setName(dto.getName());
        entity.setLevel(dto.getLevel());
        entity.setIssuedBy(dto.getIssuedBy());
        entity.setIssuedDate(dto.getIssuedDate());
        entity.setExpireDate(dto.getExpireDate());
        entity.setNote(dto.getNote());
        return entity;
    }
}