package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotNull
    @Size(max = 100)
    private String email; // thường là work email

    // Mật khẩu hệ thống tự generate hoặc admin tự nhập
    private String password;
    @NotNull
    private String role; // tham chiếu tới bảng Role

    @NotNull
    private UUID employeeId; // tham chiếu tới bảng EmployeeProfile

}
