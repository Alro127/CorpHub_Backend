package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeAdministrativeInfo;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeAdministrativeInfoResponse {

    private String identityNumber;
    private LocalDate identityIssuedDate;
    private String identityIssuedPlace;

    private String taxCode;
    private String socialInsuranceNumber;

    private String bankAccountNumber;
    private String bankName;

    private String maritalStatus;

    private String note;

    public static HrEmployeeAdministrativeInfoResponse toAdministrativeInfo(EmployeeAdministrativeInfo info) {
        if (info == null) return new HrEmployeeAdministrativeInfoResponse(); // rỗng nếu chưa tạo

        return new HrEmployeeAdministrativeInfoResponse(
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
