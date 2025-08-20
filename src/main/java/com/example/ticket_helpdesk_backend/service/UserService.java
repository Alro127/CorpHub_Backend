package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.repository.UserDbRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserDbRepository userDbRepository;

    @Autowired
    ModelMapper modelMapper;

    public List<NameInfoDto> getNameInfo() {
        return userDbRepository.findAll().stream()
                .map(user -> modelMapper.map(user, NameInfoDto.class))
                .collect(Collectors.toList());
    }
}
