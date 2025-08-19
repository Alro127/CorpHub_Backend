package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.TicketDto;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<TicketDto> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketDto.class))
                .collect(Collectors.toList());
    }
}
