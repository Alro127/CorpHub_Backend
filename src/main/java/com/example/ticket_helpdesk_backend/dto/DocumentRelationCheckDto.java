package com.example.ticket_helpdesk_backend.dto;


import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRelationCheckDto {

    private boolean hasRelations;

    /**
     * Danh sách các chứng chỉ (competency) có liên quan đến tài liệu này.
     */
    private List<RelatedCompetencyDto> relatedCompetencies;

    /**
     * DTO con mô tả thông tin chứng chỉ liên quan.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedCompetencyDto {
        private String id;
        private String name;
        private LocalDateTime uploadedDate;
    }

    /**
     * Factory tiện lợi để chuyển từ danh sách Entity sang DTO.
     */
    public static DocumentRelationCheckDto fromEntities(List<EmployeeCompetency> competencies) {
        boolean hasRelations = !competencies.isEmpty();
        List<RelatedCompetencyDto> related = competencies.stream()
                .map(c -> new RelatedCompetencyDto(
                        c.getId().toString(),
                        c.getName(),
                        c.getUpdatedDate()
                ))
                .toList();
        return new DocumentRelationCheckDto(hasRelations, related);
    }
}

