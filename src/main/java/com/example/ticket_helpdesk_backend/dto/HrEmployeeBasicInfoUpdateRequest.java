package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class HrEmployeeBasicInfoUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    private LocalDate dob;

    @Size(max = 10)
    private String gender;
}

