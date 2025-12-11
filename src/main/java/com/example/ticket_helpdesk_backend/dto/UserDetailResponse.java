package com.example.ticket_helpdesk_backend.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private UUID id;
    private String username;
    private Boolean active;
    private String roleName;

    private EmployeeSummary employee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeSummary {
        private UUID id;
        private String fullName;
        private String gender;
        private String phone;
        private String personalEmail;
        private String avatarUrl;
        private String departmentName;
        private String positionName;


    }
}
