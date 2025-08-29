package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import com.example.ticket_helpdesk_backend.dto.TicketCategoryDto;
import com.example.ticket_helpdesk_backend.dto.TicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.TicketCategoryRepository;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketByDepartmentId(UUID id) throws ResourceNotFoundException {
        List<TicketResponse> tickets = ticketRepository.findByDepartmentId(id)
                .stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();

        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("No tickets found for department " + id);
        }

        return tickets;
    }


    public List<TicketResponse> getMyTicket(UUID id) {
        return ticketRepository.findMyTicketsByRequesterId(id).stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

//    public List<TicketResponse> searchTickets(String title, Integer category, String status, String priority,
//                                              Integer requesterId, Integer assignedToId) {
//        return ticketRepository.searchTickets(title, category, status, priority, requesterId, assignedToId).stream()
//                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
//                .collect(Collectors.toList());
//    }

    public List<TicketCategoryDto> getCategories() {
        return ticketCategoryRepository.findAll().stream()
                .map((element) -> modelMapper.map(element, TicketCategoryDto.class))
                .collect(Collectors.toList());
    }

    public TicketResponse createOrUpdateTicket(TicketRequest ticketRequest) {
        Ticket ticket;
        if (ticketRequest.getId() != null) {
            // Update
            ticket = ticketRepository.findById(ticketRequest.getId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
        } else {
            // Create mới
            ticket = new Ticket();
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setStatus(TicketStatus.WAITING);
        }
        ticket.setTitle(ticketRequest.getTitle());
        ticket.setDescription(ticketRequest.getDescription());
        ticket.setPriority(ticketRequest.getPriority());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCategory(ticketCategoryRepository.findById(ticketRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
        ticket.setRequester(userRepository.findById(ticketRequest.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found")));
        ticket.setDepartment(departmentRepository.findById(ticketRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found")));

        if (ticketRequest.getAssigneeId() != null) {
            ticket.setAssignee(userRepository.findById(ticketRequest.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found")));
        } else {
            ticket.setAssignee(null);
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        return modelMapper.map(savedTicket, TicketResponse.class);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("Ticket id \" + id + \" does not exist");
        }
        ticketRepository.deleteById(id);
    }

    @Transactional
    public void deleteMany(List<UUID> ids) {
        List<UUID> missing = ids.stream()
                .filter(id -> !ticketRepository.existsById(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new RuntimeException("Tickets not found: " + missing);
        }
        ticketRepository.deleteAllById(ids);
    }
}
