package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

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
}
