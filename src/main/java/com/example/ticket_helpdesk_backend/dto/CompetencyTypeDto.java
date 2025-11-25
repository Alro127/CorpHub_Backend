package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.CompetencyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyTypeDto {

    private UUID id;
    private String code;
    private String name;
    private List<CompetencyLevelDto> levels;

    public static CompetencyTypeDto fromEntity(CompetencyType entity) {
        if (entity == null) return null;
        CompetencyTypeDto dto = new CompetencyTypeDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());

        if (entity.getLevels() != null) {
            dto.setLevels(
                    entity.getLevels().stream()
                            .map(CompetencyLevelDto::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    public CompetencyType toEntity() {
        CompetencyType entity = new CompetencyType();
        entity.setId(this.id);
        entity.setCode(this.code);
        entity.setName(this.name);
        return entity;
    }
}
