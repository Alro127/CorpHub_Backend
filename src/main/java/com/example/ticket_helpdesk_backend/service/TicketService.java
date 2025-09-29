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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

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
    UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    // ================== Helper Methods ==================

    private Ticket getTicket(UUID id) throws ResourceNotFoundException {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));
    }

    private Ticket createTicket(TicketRequest ticketRequest, UUID userId) {
        Ticket ticket = new Ticket();
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.OPEN);
        return updateTicket(ticket, ticketRequest, userId);
    }

    private Ticket updateTicket(Ticket ticket, TicketRequest ticketRequest, UUID userId) {
        if (ticketRequest.getId() != null && ticket.getStatus() != TicketStatus.OPEN) {
            throw new RuntimeException("Ticket status is not OPEN status");
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

        log.debug("UserId from token: {}", userId);

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

        return ticket;
    }

    // ================== Services ==================
    public List<TicketResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getReceivedTicketByDepartmentId(String token) throws ResourceNotFoundException {
        User user = userService.getUserFromToken(token);
        UUID departmentId = Optional.ofNullable(user.getEmployeeProfile().getDepartment())
                .orElseThrow(() -> new ResourceNotFoundException("User has no department assigned"))
                .getId();

        return ticketRepository.findReceivedTicketByDepartmentId(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TicketResponse> getSentTicketByDepartmentId(String token) throws ResourceNotFoundException {
        User user = userService.getUserFromToken(token);
        UUID departmentId = Optional.ofNullable(user.getEmployeeProfile().getDepartment())
                .orElseThrow(() -> new ResourceNotFoundException("User has no department assigned"))
                .getId();

        return ticketRepository.findSentTicketByDepartmentId(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TicketResponse> getMyTicket(UUID id) {
        return ticketRepository.findMyTickets(id).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public TicketResponse toResponse(Ticket ticket) {
        TicketResponse dto = new TicketResponse();
        dto.setId(ticket.getId());
        dto.setCategory(new TicketCategoryDto(ticket.getCategory().getId(), ticket.getCategory().getName()));
        dto.setDepartment(new DepartmentDto(ticket.getDepartment().getId(), ticket.getDepartment().getName(), ticket.getDepartment().getDescription()));
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority().name());
        dto.setStatus(ticket.getStatus().name());
        dto.setAssignedAt(ticket.getAssignedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        // requester (luôn có)
        User requester = ticket.getRequester();
        dto.setRequester(new NameInfoDto(
                requester.getId(),
                requester.getEmployeeProfile().getFullName()
        ));

        // assignee (có thể null)
        User assignee = ticket.getAssignee();
        if (assignee != null) {
            dto.setAssignee(new NameInfoDto(
                    assignee.getId(),
                    assignee.getEmployeeProfile().getFullName()
            ));
        } else {
            dto.setAssignee(null);
        }

        return dto;
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

    public TicketResponse createOrUpdateTicket(TicketRequest ticketRequest, UUID userId) throws ResourceNotFoundException {
        Ticket ticket;
        if (ticketRequest.getId() != null) {
            ticket = getTicket(ticketRequest.getId());
            ticket = updateTicket(ticket, ticketRequest, userId);
        } else {
            ticket = createTicket(ticketRequest, userId);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        return toResponse(savedTicket);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("Ticket id " + id + " does not exist");
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

    @Transactional
    public void assign(AssignTicketRequest request) throws ResourceNotFoundException {
        Ticket ticket = getTicket(request.getTicketId());
        if (EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.DONE).contains(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not valid status");
        }
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.ASSIGNING);
        ticket.setAssignedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    @Transactional
    public void confirm(UUID ticketId) throws ResourceNotFoundException {
        Ticket ticket = getTicket(ticketId);
        if (!ticket.getStatus().equals(TicketStatus.OPEN)) {
            throw new RuntimeException("Ticket must be OPEN to confirm");
        }
        ticket.setStatus(TicketStatus.WAITING);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void reject(TicketRejectionDto request, UUID userId) throws ResourceNotFoundException {
        Ticket ticket = getTicket(request.getTicketId());
        if (EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.DONE).contains(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not valid status");
        }
        ticket.setStatus(TicketStatus.REJECTED);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketRejection ticketRejection = new TicketRejection();
        ticketRejection.setTicket(ticket);
        ticketRejection.setReason(request.getReason());
        ticketRejection.setRejectedBy(user);
        ticketRejection.setRejectedAt(LocalDateTime.now());

        ticketRepository.save(ticket);
        ticketRejectionRepository.save(ticketRejection);
    }

    @Transactional
    public void takeOver(UUID ticketId) throws ResourceNotFoundException {
        Ticket ticket = getTicket(ticketId);
        if (!ticket.getStatus().equals(TicketStatus.ASSIGNING)) {
            throw new RuntimeException("Ticket must be in ASSIGNING state to take over");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);
    }

    @Transactional
    public void complete(UUID ticketId) throws ResourceNotFoundException {
        Ticket ticket = getTicket(ticketId);
        if (!ticket.getStatus().equals(TicketStatus.IN_PROGRESS)) {
            throw new RuntimeException("Ticket must be in IN_PROGRESS state to complete");
        }
        ticket.setStatus(TicketStatus.DONE);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }
}
