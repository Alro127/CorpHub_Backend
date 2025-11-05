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
public class DepartmentUsersGroupDto {
    private UUID id;
    private String name;
    private List<UserBasicDto> users;
}
