package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PositionInDepartmentDto {

    private UUID id;
    private String name;
    private String code;
    private String description;
    private Integer levelOrder;
}
