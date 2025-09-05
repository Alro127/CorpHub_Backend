package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LoginRequest;
import com.example.ticket_helpdesk_backend.dto.LoginResponse;
import com.example.ticket_helpdesk_backend.dto.RegisterRequest;
import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.Role;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.AuthException;
import com.example.ticket_helpdesk_backend.repository.AccountRepository;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
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
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleRepository roleRepository;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("User không tồn tại"));

        Account account = accountRepository.findById(user.getId())
                .orElseThrow(() -> new AuthException("Account không tồn tại"));

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
        return new LoginResponse(user.getId(), user.getFullname(), user.getEmail(), account.getRole().getName(), token );
    }

    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Department department = departmentRepository.findById(registerRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department không tồn tại"));

        User user = new User();
        user.setFullname(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setDepartment(department);

        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setRole(roleRepository.findByName(registerRequest.getRole()).orElseThrow(() -> new RuntimeException("Role không tồn tại")));
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        account.setActive(true);

        accountRepository.save(account);

        return true;
    }
}
