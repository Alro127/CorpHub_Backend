package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.dto.UserDbDto;
import com.example.ticket_helpdesk_backend.entity.UserDb;
import com.example.ticket_helpdesk_backend.repository.UserDbRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public UserDbDto getUserDtoByEmail(String email) {
        return userDbRepository.findUserDbByEmail(email).map((element) -> modelMapper.map(element, UserDbDto.class)).orElse(null);
    }

    public UserDb getUserByEmail(String email) {
        return userDbRepository.findUserDbByEmail(email).orElse(null);
    }

    @Transactional
    public UserDb saveUser(UserDb user) {
        return userDbRepository.save(user);
    }

}
