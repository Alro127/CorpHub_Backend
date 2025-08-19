package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.TicketDto;
import com.example.ticket_helpdesk_backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/get-all")
    public ResponseEntity<List<TicketDto>> getAll() {
        try {
            List<TicketDto> ticketDtoList = ticketService.getAll();
            return new ResponseEntity<>(ticketDtoList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
