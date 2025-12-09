package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeAdministrativeInfoUpdateRequest {

    @Size(max = 20)
    private String identityNumber;

    private LocalDate identityIssuedDate;

    @Size(max = 100)
    private String identityIssuedPlace;

    @Size(max = 50)
    private String taxCode;

    @Size(max = 50)
    private String socialInsuranceNumber;

    @Size(max = 50)
    private String bankAccountNumber;

    @Size(max = 100)
    private String bankName;

    @Size(max = 50)
    private String maritalStatus;

    @Size(max = 255)
    private String note;
}
