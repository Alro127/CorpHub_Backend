package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.TicketPriority;
import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.TicketRejection;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.DynamicSearchUtil;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
    TicketRejectionRepository ticketRejectionRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private ModelMapper modelMapper;

    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getReceivedTicketByDepartmentId(String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getDepartment() == null) {
            throw new ResourceNotFoundException("User with id " + userId + " has no department assigned");
        }

        UUID departmentId = user.getDepartment().getId();

        List<TicketResponse> tickets = ticketRepository.findReceivedTicketByDepartmentId(departmentId)
                .stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();

        return tickets;
    }
    public List<TicketResponse> getSentTicketByDepartmentId(String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getDepartment() == null) {
            throw new ResourceNotFoundException("User with id " + userId + " has no department assigned");
        }

        UUID departmentId = user.getDepartment().getId();

        List<TicketResponse> tickets = ticketRepository.findSentTicketByDepartmentId(departmentId)
                .stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();

        return tickets;
    }



    public List<TicketResponse> getMyTicket(UUID id) {
        return ticketRepository.findMyTickets(id).stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .collect(Collectors.toList());
    }

    @Autowired
    private DynamicSearchUtil dynamicSearchService;

    private static final Set<String> ALLOWED_FILTERS = Set.of("status", "priority", "assignee");
    private static final Set<String> ALLOWED_SORTS = Set.of("createDate", "priority", "status");

    public List<Ticket> searchTickets(TicketFilterRequest request) {
        return dynamicSearchService.search(
                Ticket.class,
                request.getFilters(),
                request.getSort(),
                request.getPagination(),
                ALLOWED_FILTERS,
                ALLOWED_SORTS
        );
    }

    public List<TicketCategoryDto> getCategories() {
        return ticketCategoryRepository.findAll().stream()
                .map((element) -> modelMapper.map(element, TicketCategoryDto.class))
                .collect(Collectors.toList());
    }

    public TicketResponse createOrUpdateTicket(TicketRequest ticketRequest, UUID userId) {
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
        ticket.setPriority(TicketPriority.valueOf(ticketRequest.getPriority()));
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setCategory(ticketCategoryRepository.findById(ticketRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        System.out.println("UserId from token: " + userId);
        ticket.setRequester(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
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
        if (ticket.getStatus().equals(TicketStatus.IN_PROGRESS) || ticket.getStatus().equals(TicketStatus.DONE)) {
            throw new RuntimeException("Ticket is not valid status");
        }
        User assignee = userRepository.findById(request.getAssigneeId()).orElseThrow(() -> new RuntimeException("Assigned user not found"));
        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.ASSIGNING);
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

    @Transactional
    public void reject(TicketRejectionDto request, UUID userId) {
        Ticket ticket = ticketRepository.findById(request.getTicketId()).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (ticket.getStatus().equals(TicketStatus.IN_PROGRESS) || ticket.getStatus().equals(TicketStatus.DONE)) {
            throw new RuntimeException("Ticket is not valid status");
        }
        ticket.setStatus(TicketStatus.REJECTED);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        TicketRejection ticketRejection = new TicketRejection();
        ticketRejection.setTicket(ticket);
        ticketRejection.setReason(request.getReason());
        ticketRejection.setRejectedBy(user);
        ticketRejection.setRejectedAt(LocalDateTime.now());

        ticketRepository.save(ticket);
        ticketRejectionRepository.save(ticketRejection);
    }

    public void takeOver(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        if (!ticket.getStatus().equals(TicketStatus.ASSIGNING)) {
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
