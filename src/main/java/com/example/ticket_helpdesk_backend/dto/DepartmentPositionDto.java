package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Department;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DepartmentPositionDto {

    private UUID departmentId;
    private String departmentName;

    private List<PositionInDepartmentDto> positions;

}
