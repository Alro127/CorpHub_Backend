package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.CompetencyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyLevelDto {

    private UUID id;
    private String name;       // Tên cấp độ (Beginner, A1, Advanced,…)
    private Integer valueScale; // Thang giá trị (1–5, dùng cho biểu đồ radar)

    public static CompetencyLevelDto fromEntity(CompetencyLevel entity) {
        if (entity == null) return null;
        return new CompetencyLevelDto(
                entity.getId(),
                entity.getName(),
                entity.getValueScale()
        );
    }

    public CompetencyLevel toEntity() {
        CompetencyLevel entity = new CompetencyLevel();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setValueScale(this.valueScale);
        return entity;
    }
}
