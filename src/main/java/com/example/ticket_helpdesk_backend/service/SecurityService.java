package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("securityService")
public class SecurityService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketRepository ticketRepository;

    public SecurityService(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.ticketRepository = ticketRepository;
    }

    /** Lấy email hiện tại */
    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /** Lấy user hiện tại */
    private Optional<User> getCurrentUser() {
        return userRepository.findByUsername(getCurrentEmail());
    }

    /** Lấy ticket theo id */
    private Optional<Ticket> getTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId);
    }

    /** Lấy department theo id */
    private Optional<Department> getDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId);
    }

    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isManagerOfDepartment(UUID departmentId) {
        return getCurrentUser()
                .filter(user -> "ROLE_MANAGER".equals(user.getRole().getName()))
                .filter(user -> user.getEmployeeProfile().getDepartment() != null
                        && user.getEmployeeProfile().getDepartment().getId().equals(departmentId))
                .isPresent();
    }


    public boolean isManagerReceiveTicket(UUID ticketId) {
        return getCurrentUser()
                .filter(user -> isManagerOfDepartment(user.getEmployeeProfile().getDepartment().getId()))
                .filter(user -> getTicket(ticketId)
                        .map(Ticket::getDepartment)
                        .map(dep -> dep.equals(user.getEmployeeProfile().getDepartment()))
                        .orElse(false))
                .isPresent();
    }

    public boolean isManagerOfTicketOwner(UUID ticketId) {
        return getCurrentUser()
                .filter(user -> isManagerOfDepartment(user.getEmployeeProfile().getDepartment().getId()))
                .filter(user -> getTicket(ticketId)
                        .map(Ticket::getRequester)
                        .map(u -> u.getEmployeeProfile().getDepartment())
                        .map(dep -> dep.equals(user.getEmployeeProfile().getDepartment()))
                        .orElse(false))
                .isPresent();
    }

    public boolean isAssigneeOfTicket(UUID ticketId) {
        return getCurrentUser()
                .filter(user -> getTicket(ticketId)
                        .map(Ticket::getAssignee)
                        .map(assignee -> assignee.getId().equals(user.getId()))
                        .orElse(false))
                .isPresent();
    }
}


