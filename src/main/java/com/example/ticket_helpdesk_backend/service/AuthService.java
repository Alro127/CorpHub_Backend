package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LoginRequest;
import com.example.ticket_helpdesk_backend.dto.RegisterRequest;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.UserDb;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    public String login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassWord())
        );

        String role = auth.getAuthorities().iterator().next().getAuthority();

        return jwtUtil.generateToken(request.getEmail(), role);
    }

    public boolean register(RegisterRequest registerRequest) {
        if (userService.getUserByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Department department = departmentService.getDepartmentById(registerRequest.getDepartmentId());

        UserDb user = new UserDb();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        String role = registerRequest.getRole();
        if (role == null || (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN"))) {
            role = "ROLE_USER";
        }
        user.setRole(role.trim()); // loại bỏ khoảng trắng
        user.setPassWord(passwordEncoder.encode(registerRequest.getPassWord()));
        user.setStatus("offline");
        user.setDepartment(department);

        UserDb savedUser = userService.saveUser(user);
        return savedUser != null;
    }
}
