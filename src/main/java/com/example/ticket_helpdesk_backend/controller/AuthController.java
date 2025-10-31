package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.LoginRequest;
import com.example.ticket_helpdesk_backend.dto.LoginResponse;
import com.example.ticket_helpdesk_backend.exception.AuthException;
import com.example.ticket_helpdesk_backend.service.AuthService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        var result = authService.login(request);

        // Gửi refreshToken vào cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false) // true nếu chạy HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        ApiResponse<LoginResponse> responseBody = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login successfully",
                LocalDateTime.now(),
                result.getUserInfo()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException("Refresh token cookie is missing");
        }

        System.out.println("Refresh token: " + refreshToken);

        LoginResponse loginResponse = authService.refresh(refreshToken);

        ApiResponse<LoginResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Token refreshed successfully",
                LocalDateTime.now(),
                loginResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ApiResponse<String> res = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Logout successful",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }



}
