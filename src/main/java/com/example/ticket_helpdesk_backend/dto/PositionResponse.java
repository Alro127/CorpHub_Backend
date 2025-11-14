package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.Position;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Data
@Getter
@Setter
public class PositionResponse {
    private UUID id;
    private DepartmentDto departmentDto;
    private String name;
    private String code;
    private String description;
    private Integer levelOrder;

    public static PositionResponse fromEntity(Position position) {
        PositionResponse res = new PositionResponse();
        res.setId(position.getId());
        res.getDepartmentDto().setId(position.getDepartment().getId());
        res.getDepartmentDto().setName(position.getDepartment().getName());
        res.getDepartmentDto().setDescription(position.getDepartment().getDescription());
        res.setName(position.getName());
        res.setCode(position.getCode());
        res.setDescription(position.getDescription());
        res.setLevelOrder(position.getLevelOrder());

        return res;
    }
}
