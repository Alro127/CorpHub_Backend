package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Size(max = 20)
    private String phone;

    @NotNull
    @Size(max = 20)
    private String role;

    @NotNull
    @Size(max = 200)
    private String passWord;

    @NotNull
    private Integer departmentId;
}
