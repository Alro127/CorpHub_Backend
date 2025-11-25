package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentDetailDto {
    private UUID id;
    private String name;
    private String description;

    private UserDepartmentDto manager;
    private List<UserDepartmentDto> users;

    private List<DepartmentDetailDto> children;
}




