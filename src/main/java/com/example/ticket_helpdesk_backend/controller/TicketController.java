package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.TicketCategoryDto;
import com.example.ticket_helpdesk_backend.dto.TicketRequest;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAll() {
        List<TicketResponse> ticketResponseList = ticketService.getAll();
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All tickets found",
                LocalDateTime.now(),
                ticketResponseList
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-ticket/{id}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTicket(@PathVariable Integer id) {
        List<TicketResponse> ticketResponseList = ticketService.getMyTicket(id);
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "My tickets found",
                LocalDateTime.now(),
                ticketResponseList
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> searchTickets(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Integer requesterId,
            @RequestParam(required = false) Integer assignedToId
/*            @RequestParam(required = false) LocalDateTime createdAt,
            @RequestParam(required = false) LocalDateTime updatedAt*/
    ) {
        List<TicketResponse> ticketResponseList = ticketService.searchTickets(title, category,status, priority, requesterId, assignedToId);
        ApiResponse<List<TicketResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "My tickets found",
                LocalDateTime.now(),
                ticketResponseList
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TicketCategoryDto>>> getCategories() {
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
    public ResponseEntity<ApiResponse<TicketResponse>> createOrUpdate(@RequestBody TicketRequest request) {
        ApiResponse<TicketResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Ticket saved successfully",
                LocalDateTime.now(),
                ticketService.createOrUpdateTicket(request)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        ticketService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteMany(@RequestParam("ids") List<Integer> ids) {
        ticketService.deleteMany(ids);
        return ResponseEntity.noContent().build();
    }
}
