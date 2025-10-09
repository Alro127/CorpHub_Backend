package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.TicketService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<?> getAll() {
        List<TicketResponse> ticketResponseList = ticketService.getAll();
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All tickets found",
                LocalDateTime.now(),
                ticketResponseList
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/department/received")
    public ResponseEntity<?> getByDepartmentId(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String keyword
    ) throws ResourceNotFoundException {
        String token = authHeader.substring(7);

        Page<TicketResponse> pageData = ticketService.getReceivedTicketByDepartmentId(
                token, page, size, status, priority, categoryId, from, to, keyword
        );
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Received tickets found",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('MANAGER')")
    @GetMapping("/department/sent")
    public ResponseEntity<?> getSentTicketByDepartmentId(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String keyword
    ) throws ResourceNotFoundException {
        String token = authHeader.substring(7);

        Page<TicketResponse> pageData = ticketService.getSentTicketByDepartmentId(
                token, page, size, status, priority, categoryId, from, to, keyword
        );

        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Sent tickets found",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<?> getMyTickets(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isRequester,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String keyword
    ) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        Page<TicketResponse> pageData = ticketService.getMyTicket(
                userId, page, size, isRequester, status, priority, categoryId, from, to, keyword
        );

        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "My tickets found",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<TicketCategoryDto> ticketCategoryDtoList = ticketService.getCategories();
        ApiResponse<List<TicketCategoryDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All categories found",
                LocalDateTime.now(),
                ticketCategoryDtoList
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<?> createOrUpdate(@Valid @RequestBody TicketRequest request, @RequestHeader("Authorization") String authHeader) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        ticketService.validateBusinessRules(request);

        ApiResponse<TicketResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket saved successfully",
                LocalDateTime.now(),
                ticketService.createOrUpdateTicket(request, userId)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        boolean deleted = ticketService.deleteById(id);

        ApiResponse<Boolean> response = new ApiResponse<>(
                deleted ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value(),
                deleted ? "Ticket deleted successfully" : "Failed to delete ticket",
                LocalDateTime.now(),
                deleted
        );

        return ResponseEntity.status(deleted ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMany(@RequestParam("ids") List<UUID> ids) {
        ticketService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.isManagerReceiveTicket(#request.ticketId)")
    @PostMapping("/assign")
    public ResponseEntity<?> assign(@RequestBody AssignTicketRequest request) throws ResourceNotFoundException {
        ticketService.assign(request);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket assigned successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.isManagerOfTicketOwner(#ticketId)")
    @PostMapping("/confirm/{ticketId}")
    public ResponseEntity<?> confirm(@PathVariable UUID ticketId) throws ResourceNotFoundException {
        ticketService.confirm(ticketId);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket confirmed successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.isManagerOfTicketOwner(#request.ticketId) or @securityService.isAssigneeOfTicket(#request.ticketId)")
    @PostMapping("/reject")
    public ResponseEntity<?> reject(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody TicketRejectionDto request) throws ResourceNotFoundException {
        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.getUserId(token);

        ticketService.reject(request, userId);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket rejected successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.isAssigneeOfTicket(#ticketId)")
    @PostMapping("/take-over/{ticketId}")
    public ResponseEntity<?> takeOver(@PathVariable UUID ticketId) throws ResourceNotFoundException {
        ticketService.takeOver(ticketId);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket taken over successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.isAssigneeOfTicket(#ticketId) or @securityService.isTicketOwner(#ticketId)")
    @PostMapping("/complete/{ticketId}")
    public ResponseEntity<?> complete(@RequestHeader("Authorization") String authHeader, @PathVariable UUID ticketId) throws ResourceNotFoundException {
        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.getUserId(token);

        ticketService.complete(ticketId, userId);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket completed successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
}
