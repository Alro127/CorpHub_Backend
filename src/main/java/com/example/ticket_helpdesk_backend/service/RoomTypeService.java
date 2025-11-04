package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.RoomTypeDto;
import com.example.ticket_helpdesk_backend.entity.RoomType;
import com.example.ticket_helpdesk_backend.repository.RoomRepository;
import com.example.ticket_helpdesk_backend.repository.RoomTypeRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomTypeService {
    private RoomTypeRepository roomTypeRepository;
    private ModelMapper modelMapper;

    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream().map(rt -> modelMapper.map(rt, RoomTypeDto.class)).collect(Collectors.toList());
    }
}
