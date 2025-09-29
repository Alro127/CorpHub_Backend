package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public class CreateEmployeeProfileRequest {
    @NotNull
    @Size(max = 200)
    private String fullName;

    private LocalDate dob;

    private String gender;

    @Size(max = 20)
    private String phone;

    @Email
    private String personalEmail;

    private String avatar; // URL

    @NotNull
    private UUID departmentId; // tham chiếu đến bảng Department
}
