package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.UserDb;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDbDto implements Serializable {
    Integer id;
    @NotNull
    DepartmentBasicInfoDto department;
    @NotNull
    @Size(max = 100)
    String fullName;
    @NotNull
    @Size(max = 20)
    String role;
    @NotNull
    @Size(max = 150)
    String email;
    @Size(max = 20)
    String phone;
    @NotNull
    @Size(max = 10)
    String status;
}