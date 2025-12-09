package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class EmployeeContactInfoUpdateDto {

    @Email
    @Size(max = 100)
    private String personalEmail;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 2000)
    private String about;

    public static EmployeeContactInfoUpdateDto fromEntity(EmployeeProfile employeeProfile) {
        EmployeeContactInfoUpdateDto dto = new EmployeeContactInfoUpdateDto();
        dto.setPersonalEmail(employeeProfile.getPersonalEmail());
        dto.setPhone(employeeProfile.getPhone());
        dto.setAddress(employeeProfile.getAddress());
        dto.setAbout(employeeProfile.getAbout());
        return dto;
    }
}

