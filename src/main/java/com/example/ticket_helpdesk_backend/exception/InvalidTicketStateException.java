package com.example.ticket_helpdesk_backend.exception;

public class InvalidTicketStateException extends RuntimeException {
    public InvalidTicketStateException(String message) {
        super(message);
    }
}
