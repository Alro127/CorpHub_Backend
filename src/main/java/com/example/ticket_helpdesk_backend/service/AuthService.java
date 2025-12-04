package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LoginRequest;
import com.example.ticket_helpdesk_backend.dto.LoginResponse;
import com.example.ticket_helpdesk_backend.dto.LoginResult;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.AuthException;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    FileStorageService fileStorageService;

    private static final String BUCKET_NAME = "employee-avatars";

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new AuthException("User không tồn tại"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AuthException("Sai email hoặc mật khẩu");
        }

        String role = user.getRole().getName();
        UUID userId = user.getId();

        String accessToken = null;
        String refreshToken = null;

        // Nếu tài khoản active thì sinh token
        if (Boolean.TRUE.equals(user.getActive())) {
            accessToken = jwtUtil.generateToken(request.getEmail(), userId, role);
            refreshToken = jwtUtil.generateRefreshToken(request.getEmail());
        }

        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getEmployeeProfile().getFullName(),
                user.getUsername(),
                user.getEmployeeProfile().getAvatar() != null ? fileStorageService.getPresignedUrl(BUCKET_NAME, user.getEmployeeProfile().getAvatar()) : null,
                user.getRole().getName(),
                user.getActive(),
                accessToken,
                user.getEmployeeProfile().getDepartment().getName()
        );

        return new LoginResult(loginResponse, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtUtil.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User không tồn tại"));

        String newAccessToken = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                user.getRole().getName()
        );

        return new LoginResponse(
                user.getId(),
                user.getEmployeeProfile().getFullName(),
                user.getUsername(),
                user.getEmployeeProfile().getAvatar() != null ? fileStorageService.getPresignedUrl(BUCKET_NAME, user.getEmployeeProfile().getAvatar()) : null,
                user.getRole().getName(),
                user.getActive(),
                newAccessToken,
                user.getEmployeeProfile().getDepartment().getName()
        );
    }
}
