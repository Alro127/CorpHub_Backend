package com.example.ticket_helpdesk_backend.dto;

import lombok.*;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDepartmentDto {
    private UUID userId;
    private String fullName;
    private String email;
    private String avatar;

    private UUID positionId;
    private String positionName;
    private Integer levelOrder;
}
