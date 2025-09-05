package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import com.example.ticket_helpdesk_backend.dto.AssignTicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketCategoryDto;
import com.example.ticket_helpdesk_backend.dto.TicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
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

    public List<TicketResponse> getTicketByDepartmentId(UUID userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getDepartment() == null) {
            throw new ResourceNotFoundException("User with id " + userId + " has no department assigned");
        }

        UUID departmentId = user.getDepartment().getId();

        List<TicketResponse> tickets = ticketRepository.findByDepartmentId(departmentId)
                .stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();

        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("No tickets found for department " + departmentId);
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

    public TicketResponse createOrUpdateTicket(TicketRequest ticketRequest, UUID requesterId) {
        Ticket ticket;
        if (ticketRequest.getId() != null) {
            // Update
            ticket = ticketRepository.findById(ticketRequest.getId())
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            if (ticket.getStatus() != TicketStatus.OPEN) {
                throw new RuntimeException("Ticket status is not OPEN status");
            }
        } else {
            // Create mới
            ticket = new Ticket();
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setStatus(TicketStatus.OPEN);
        }
        ticket.setTitle(ticketRequest.getTitle());
        ticket.setDescription(ticketRequest.getDescription());
        ticket.setPriority(ticketRequest.getPriority());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCategory(ticketCategoryRepository.findById(ticketRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
        ticket.setRequester(userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found")));
        ticket.setDepartment(departmentRepository.findById(ticketRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found")));

        if (ticketRequest.getAssigneeId() != null) {
            ticket.setAssignee(userRepository.findById(ticketRequest.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found")));
        } else {
            ticket.setAssignee(null);
        }
        Ticket savedTicket = null;
        try {
            savedTicket = ticketRepository.save(ticket);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

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

    public void assign(AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId()).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (!ticket.getStatus().equals(TicketStatus.WAITING)) {
            throw new RuntimeException("Ticket is not waiting status");
        }
        User assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new RuntimeException("Assigned user not found"));
        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.ACCEPTED);
        ticket.setAssignedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    public void confirm(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (!ticket.getStatus().equals(TicketStatus.OPEN)) {
            throw new RuntimeException("Ticket is not open status");
        }
        ticket.setStatus(TicketStatus.WAITING);
        ticketRepository.save(ticket);
    }

    public void takeOver(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (!ticket.getStatus().equals(TicketStatus.ACCEPTED)) {
            throw new RuntimeException("Ticket is not accepted status");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);
    }

    public void complete(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (!ticket.getStatus().equals(TicketStatus.IN_PROGRESS)) {
            throw new RuntimeException("Ticket is not in in-progress status");
        }
        ticket.setStatus(TicketStatus.DONE);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }
}
