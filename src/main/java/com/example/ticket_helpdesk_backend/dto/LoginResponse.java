package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String avatar;
    private String role;
    private Boolean active;
    private String accessToken;
    private String department;
}
