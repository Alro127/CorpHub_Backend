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

        //Khởi tạo trước deptDto để tránh bị lỗi null
        DepartmentDto deptDto = new DepartmentDto();
        deptDto.setId(position.getDepartment().getId());
        deptDto.setName(position.getDepartment().getName());
        deptDto.setDescription(position.getDepartment().getDescription());

        res.setDepartmentDto(deptDto);
        res.setName(position.getName());
        res.setCode(position.getCode());
        res.setDescription(position.getDescription());
        res.setLevelOrder(position.getLevelOrder());

        return res;
    }
}
