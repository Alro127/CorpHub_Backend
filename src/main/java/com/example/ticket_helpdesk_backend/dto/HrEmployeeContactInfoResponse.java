package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeContactInfoResponse {
    private String personalEmail;
    private String phone;
    private String address;
    private String about;

    public static HrEmployeeContactInfoResponse toContactInfo(EmployeeProfile profile) {
        return new HrEmployeeContactInfoResponse(
                profile.getPersonalEmail(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getAbout()
        );
    }
}

