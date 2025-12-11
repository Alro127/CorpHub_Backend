package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class HrEmployeeContactInfoUpdateRequest {

    @Email
    @Size(max = 100)
    private String personalEmail;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 2000)
    private String about;
}
