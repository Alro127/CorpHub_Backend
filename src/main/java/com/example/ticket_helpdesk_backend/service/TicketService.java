package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.TicketCategoryDto;
import com.example.ticket_helpdesk_backend.dto.TicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.repository.TicketCategoryRepository;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import com.example.ticket_helpdesk_backend.repository.UserDbRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Console;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserDbRepository userRepository;

    @Autowired
    TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    public List<TicketCategoryDto> getCategories() {
        return ticketCategoryRepository.findAll().stream()
                .map((element) -> modelMapper.map(element, TicketCategoryDto.class))
                .collect(Collectors.toList());
    }

    public TicketResponse createOrUpdateTicket(TicketRequest ticketRequest) {
        Ticket ticket;
        if (ticketRequest.getId() > -1) {
            // Update
            ticket = ticketRepository.findById(ticketRequest.getId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
        } else {
            // Create mới
            ticket = new Ticket();
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setStatus(ticketRequest.getStatus() != null ? ticketRequest.getStatus() : "open");
        }
        ticket.setTitle(ticketRequest.getTitle());
        ticket.setDescription(ticketRequest.getDescription());
        ticket.setPriority(ticketRequest.getPriority());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCategory(ticketCategoryRepository.findById(ticketRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
        ticket.setRequester(userRepository.findById(ticketRequest.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found")));
        if (ticketRequest.getAssignedToId() != null) {
            ticket.setAssignedTo(userRepository.findById(ticketRequest.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found")));
        } else {
            ticket.setAssignedTo(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        System.out.println(ticketRequest.getAssignedToId());
        //System.out.println(savedTicket.getAssignedTo().getId());

        return modelMapper.map(savedTicket, TicketResponse.class);
    }
}
