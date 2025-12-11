package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeAdministrativeInfo;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeAdministrativeInfoDto {

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

    public static EmployeeAdministrativeInfoDto fromEntity(EmployeeAdministrativeInfo info) {
        if (info == null) return null;

        return new EmployeeAdministrativeInfoDto(
                info.getIdentityNumber(),
                info.getIdentityIssuedDate(),
                info.getIdentityIssuedPlace(),
                info.getTaxCode(),
                info.getSocialInsuranceNumber(),
                info.getBankAccountNumber(),
                info.getBankName(),
                info.getMaritalStatus(),
                info.getNote()
        );
    }
}
