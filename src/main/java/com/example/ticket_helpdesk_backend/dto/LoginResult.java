package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {
    private LoginResponse userInfo; // Dữ liệu user trả về cho FE
    private String refreshToken;    // Token dùng để set cookie
}