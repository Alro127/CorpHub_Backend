package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.TicketCommentRequest;
import com.example.ticket_helpdesk_backend.dto.TicketCommentResponse;
import com.example.ticket_helpdesk_backend.entity.TicketComment;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.TicketCommentRepository;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import jakarta.security.auth.message.AuthException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketCommentService {
    private final ModelMapper modelMapper;

    private final TicketCommentRepository ticketCommentRepository;

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    public TicketCommentService(ModelMapper modelMapper, TicketCommentRepository ticketCommentRepository, TicketRepository ticketRepository, TicketService ticketService) {
        this.modelMapper = modelMapper;
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }

    public TicketComment getTicketComment(UUID id) throws ResourceNotFoundException {
        return ticketCommentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TicketComment not found with id " + id));
    }

    public List<TicketCommentResponse> getCommentsByTicketId(UUID ticketId) throws ResourceNotFoundException {
        if (ticketId == null || !ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Không tìm thấy ticket");
        }
        return ticketCommentRepository.findByTicketIdAndIsDeletedFalse(ticketId).stream().map(TicketCommentResponse::toResponse).collect(Collectors.toList());
    }

    public TicketCommentResponse save(TicketCommentRequest ticketCommentRequest, User user) throws ResourceNotFoundException {
        if (ticketCommentRequest == null || user == null) {
            throw new RuntimeException("Ticket comment request or user is null");
        }

        TicketComment ticketComment;

        if (ticketCommentRequest.getId() == null) {
            // Create
            ticketComment = new TicketComment();
            ticketComment.setCreatedAt(LocalDateTime.now());
        } else {
            // Update
            ticketComment = getTicketComment(ticketCommentRequest.getId());
        }

        ticketComment.setTicket(ticketService.getTicket(ticketCommentRequest.getTicketId()));

        if (ticketCommentRequest.getParentId() != null) {
            ticketComment.setParent(ticketCommentRepository
                    .findById(ticketCommentRequest.getParentId())
                    .orElse(null));
        } else {
            ticketComment.setParent(null);
        }

        ticketComment.setUser(user);
        ticketComment.setCommentText(ticketCommentRequest.getCommentText());
        ticketComment.setUpdatedAt(LocalDateTime.now());
        ticketComment.setIsDeleted(false);

        TicketComment saved = ticketCommentRepository.save(ticketComment);
        return TicketCommentResponse.toResponse(saved);
    }

    public boolean deleteTicketComment(UUID id, User user) throws ResourceNotFoundException, AuthException {

        TicketComment ticketComment = getTicketComment(id);

        if (!user.getId().equals(ticketComment.getUser().getId())) {
            throw new AuthException("Không đúng người dùng");
        }

        ticketComment.setIsDeleted(true);
        ticketComment.setUpdatedAt(LocalDateTime.now());
        ticketCommentRepository.save(ticketComment);
        return true;
    }
}
