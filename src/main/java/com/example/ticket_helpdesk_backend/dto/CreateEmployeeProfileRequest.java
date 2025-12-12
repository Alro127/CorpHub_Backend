package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeProfileRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @Past
    private LocalDate dob;

    @NotNull
    private String gender;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @Email
    @NotBlank
    @Size(max = 100)
    private String personalEmail;

    @NotNull
    private LocalDate joinDate;

    @NotNull
    private UUID departmentId;

    @NotNull
    private UUID positionId;
}
