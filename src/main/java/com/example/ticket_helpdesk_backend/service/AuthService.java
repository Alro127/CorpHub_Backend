package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LoginRequest;
import com.example.ticket_helpdesk_backend.dto.LoginResponse;

import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.AuthException;

import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new AuthException("User không tồn tại"));

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AuthException("Sai email hoặc mật khẩu");
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        UUID userId = user.getId();
        String token = jwtUtil.generateToken(request.getEmail(), userId, role);
        // Chỗ này chưa biết nên sửa thành mail cá nhân hay mail công ty nữa
        return new LoginResponse(user.getId(), user.getEmployeeProfile().getFullName(), user.getUsername(), user.getEmployeeProfile().getAvatar(), user.getRole().getName(), token );
    }

}
