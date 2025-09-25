package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.RegisterRequest;
import com.example.ticket_helpdesk_backend.dto.UserDataResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AccountRepository;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
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
    private AccountRepository accountRepository;
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
    public UserDto getUserDtoByEmail(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUser() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
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
            return userRepository.findByDepartment_Id(user.getDepartment().getId()).stream()
                    .map(emp -> modelMapper.map(emp, UserDto.class))
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid role " + role);
        }
    }

    public List<UserDto> getUsersBySearch(String keyword) {
        return userRepository.searchByeFullNameOrEmail(keyword).stream().map((element) -> modelMapper.map(element, UserDto.class)).collect(Collectors.toList());
    }

    @Transactional
    public boolean createUser(RegisterRequest registerRequest) {
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
    public UserDataResponse getEmployeeById(UUID userId) throws ResourceNotFoundException {
        if (userId == null) {
            throw new RuntimeException("Invalid input, user id is null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        Account account = accountRepository.findById(userId).orElse(null);

        // Chuyển đổi entity sang DTO
        return UserDataResponse.fromEntity(user, account);
    }

}
