package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotNull
    @Size(max = 100)
    private String fullName;

    @NotNull
    @Email
    @Size(max = 150)
    private String email;

    @NotNull
    private String role;

    @NotNull
    @Size(max = 200)
    private String password;

    private UUID departmentId;
}
