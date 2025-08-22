package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.TicketCategoryDto;
import com.example.ticket_helpdesk_backend.dto.TicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

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

    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.isManagerOfDepartment(#id)")
    @GetMapping("department/{id}")
    public ResponseEntity<?> getByDepartmentId(@PathVariable Integer id) throws ResourceNotFoundException {
        List<TicketResponse> ticketResponseList = ticketService.getTicketByDepartmentId(id);
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All tickets found",
                LocalDateTime.now(),
                ticketResponseList
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
    public ResponseEntity<?> createOrUpdate(@RequestBody TicketRequest request) {
        ApiResponse<TicketResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket saved successfully",
                LocalDateTime.now(),
                ticketService.createOrUpdateTicket(request)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        ticketService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMany(@RequestParam("ids") List<Integer> ids) {
        ticketService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
