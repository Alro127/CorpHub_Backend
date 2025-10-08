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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.ticket_helpdesk_backend.specification.TicketSpecifications.*;

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

    /**
     * Validate business rules for Ticket creation or update.
     * Throws IllegalArgumentException or ResourceNotFoundException on invalid data.
     */
    public void validateBusinessRules(TicketRequest req) throws ResourceNotFoundException {
        // Validate Category tồn tại
        if (!ticketCategoryRepository.existsById(req.getCategoryId())) {
            throw new ResourceNotFoundException("Category not found with ID: " + req.getCategoryId());
        }

        // Validate Department tồn tại
        if (!departmentRepository.existsById(req.getDepartmentId())) {
            throw new ResourceNotFoundException("Department not found with ID: " + req.getDepartmentId());
        }

        // Validate Priority (Enum)
        if (req.getPriority() == null) {
            throw new IllegalArgumentException("Priority must not be null.");
        }

        // Validate Assignee
        if (req.getAssigneeId() != null) {
            User assignee = userRepository.findById(req.getAssigneeId()).orElse(null);
            if (assignee == null) {
                throw new ResourceNotFoundException("Assignee not found with ID: " + req.getAssigneeId());
            }
            // Kiểm tra assignee thuộc đúng department
            boolean sameDepartment = assignee.getEmployeeProfile().getDepartment().getId().equals(req.getDepartmentId());
            if (!sameDepartment) {
                throw new IllegalArgumentException("Assignee does not belong to the specified department.");
            }
        }

        // Kiểm tra độ dài tiêu đề
        if (req.getTitle() == null || req.getTitle().isEmpty() || req.getTitle().length() > 100) {
            throw new IllegalArgumentException("A ticket title is not valid.");
        }

        // Kiểm tra độ dài description
        if (req.getDescription() != null && req.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters.");
        }
    }

    public Ticket getTicket(UUID id) throws ResourceNotFoundException {
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
                .map(TicketResponse::toResponse)
                .collect(Collectors.toList());
    }

    public Page<TicketResponse> getReceivedTicketByDepartmentId(
            String token,
            int page,
            int size,
            String status,
            String priority,
            UUID categoryId,
            LocalDate from,
            LocalDate to,
            String keyword
    ) throws ResourceNotFoundException {

        User user = userService.getUserFromToken(token);
        UUID departmentId = Optional.ofNullable(user.getEmployeeProfile().getDepartment())
                .orElseThrow(() -> new ResourceNotFoundException("User has no department assigned"))
                .getId();

        Specification<Ticket> spec = Specification
                .where(receivedByDepartment(departmentId))
                .and(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasCategory(categoryId))
                .and(createdBetween(from, to))
                .and(search(keyword));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ticketRepository.findAll(spec, pageable).map(TicketResponse::toResponse);
    }

    public Page<TicketResponse> getSentTicketByDepartmentId(
            String token,
            int page,
            int size,
            String status,
            String priority,
            UUID categoryId,
            LocalDate from,
            LocalDate to,
            String keyword
    ) throws ResourceNotFoundException {

        User user = userService.getUserFromToken(token);
        UUID departmentId = Optional.ofNullable(user.getEmployeeProfile().getDepartment())
                .orElseThrow(() -> new ResourceNotFoundException("User has no department assigned"))
                .getId();

        Specification<Ticket> spec = Specification
                .where(sentByDepartment(departmentId))
                .and(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasCategory(categoryId))
                .and(createdBetween(from, to))
                .and(search(keyword));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ticketRepository.findAll(spec, pageable).map(TicketResponse::toResponse);
    }

    public Page<TicketResponse> getMyTicket(
            UUID userId,
            int page,
            int size,
            Boolean isRequester,
            String status,
            String priority,
            UUID categoryId,
            LocalDate from,
            LocalDate to,
            String keyword
    ) {
        Specification<Ticket> spec = Specification
                .where(buildUserRoleSpec(userId, isRequester))
                .and(hasStatus(status))
                .and(hasPriority(priority))
                .and(hasCategory(categoryId))
                .and(createdBetween(from, to))
                .and(search(keyword));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return ticketRepository.findAll(spec, pageable).map(TicketResponse::toResponse);
    }

//    @Autowired
//    private DynamicSearchUtil dynamicSearchService;
//
//    private static final Set<String> ALLOWED_FILTERS = Set.of("status", "priority", "assignee");
//    private static final Set<String> ALLOWED_SORTS = Set.of("createDate", "priority", "status");
//
//    public List<Ticket> searchTickets(TicketFilterRequest request) {
//        return dynamicSearchService.search(
//                Ticket.class,
//                request.getFilters(),
//                request.getSort(),
//                request.getPagination(),
//                ALLOWED_FILTERS,
//                ALLOWED_SORTS
//        );
//    }

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
        return TicketResponse.toResponse(savedTicket);
    }

    @Transactional
    public boolean deleteById(UUID id) {
        if (!ticketRepository.existsById(id)) {
            return false; // Không tồn tại
        }
        try {
            ticketRepository.deleteById(id);
            return true; // Xóa thành công
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa ticket " + id + ": " + e.getMessage());
            return false;
        }
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.DONE).contains(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not valid status");
        }
        if (user.getRole().getName().equals("ROLE_USER"))
        {
            ticket.setStatus(TicketStatus.WAITING);
        }
        else
        {
            ticket.setStatus(TicketStatus.REJECTED);
            TicketRejection ticketRejection = new TicketRejection();
            ticketRejection.setTicket(ticket);
            ticketRejection.setReason(request.getReason());
            ticketRejection.setRejectedBy(user);
            ticketRejection.setRejectedAt(LocalDateTime.now());
            ticketRejectionRepository.save(ticketRejection);
        }
        ticketRepository.save(ticket);
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
