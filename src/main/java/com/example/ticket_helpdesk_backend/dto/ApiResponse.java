package com.example.ticket_helpdesk_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private T data;

    private Map<String, Object> meta;

    public ApiResponse(int status, String message, LocalDateTime timestamp, T data) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
        this.meta = null;
    }
}
