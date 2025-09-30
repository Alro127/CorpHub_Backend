package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    ModelMapper modelMapper;

    public User getUserFromToken(String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    @Transactional(readOnly = true)
    public List<NameInfoDto> getNameInfo() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, NameInfoDto.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserDtoByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserDto::toUserDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + username));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) throws ResourceNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUser() {
        return userRepository.findAll().stream()
                .map(UserDto::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getEmployees(String token) throws ResourceNotFoundException {
        User user = getUserFromToken(token);
        String role = jwtUtil.getRole(token);

        try {
            UserRole userRole = UserRole.valueOf(role);
            if (userRole == UserRole.ROLE_ADMIN) {
                return this.getAllUser();
            }
            return userRepository.findByEmployeeProfile_Department_Id(user.getEmployeeProfile().getDepartment().getId()).stream()
                    .map(UserDto::toUserDto)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid role " + role);
        }
    }

    public List<UserDto> getUsersBySearch(String keyword) {
        return userRepository.searchByFullNameOrUsername(keyword).stream().map(UserDto::toUserDto).collect(Collectors.toList());
    }

    @Transactional
    public boolean createUser(CreateUserRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUserName()).isPresent()) {
            throw new RuntimeException("UserName đã tồn tại");
        }

        User user = new User();
        user.setUsername(registerRequest.getUserName());
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(roleRepository.findById(registerRequest.getRoleId()).orElseThrow(() -> new RuntimeException("Role không tồn tại")));

        User savedUser = userRepository.save(user);

        return true;
    }
//    public UserDataResponse getEmployeeById(UUID userId) throws ResourceNotFoundException {
//        if (userId == null) {
//            throw new RuntimeException("Invalid input, user id is null");
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
//
//        Account account = accountRepository.findById(userId).orElse(null);
//
//        // Chuyển đổi entity sang DTO
//        return UserDataResponse.fromEntity(user, account);
//    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private String getRoleName(UUID userId) {
        User user = getUserById(userId);
        return user != null ? user.getRole().getName() : null;
    }

    public boolean isAdmin(UUID userId) {
        return "ROLE_ADMIN".equals(getRoleName(userId));
    }

    public boolean isManager(UUID userId) {
        return "ROLE_MANAGER".equals(getRoleName(userId));
    }

    public boolean isUser(UUID userId) {
        return "ROLE_USER".equals(getRoleName(userId));
    }

}
