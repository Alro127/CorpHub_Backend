package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    JwtUtil jwtUtil;

    @Autowired
    ModelMapper modelMapper;

    public List<NameInfoDto> getNameInfo() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, NameInfoDto.class))
                .collect(Collectors.toList());
    }

    public UserDto getUserDtoByEmail(String email) {
        return userRepository.findByEmail(email).map((element) -> modelMapper.map(element, UserDto.class)).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<UserDto> getAllUser() {
        return userRepository.findAll().stream().map((element) -> modelMapper.map(element, UserDto.class)).collect(Collectors.toList());
    }

    public List<UserDto> getEmployees(String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        List<UserDto> userDtoList;

        try {
            UserRole userRole = UserRole.valueOf(role);
            if (userRole == UserRole.ROLE_ADMIN) {
                userDtoList = this.getAllUser();
                return userDtoList;
            }
            userDtoList = userRepository.findByDepartment_Id(user.getDepartment().getId()).stream().map((element) -> modelMapper.map(element, UserDto.class)).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid role " + role);
        }

        return userDtoList;
    }
}
