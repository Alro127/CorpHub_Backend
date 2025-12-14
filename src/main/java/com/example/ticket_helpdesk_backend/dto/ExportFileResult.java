package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;

@Getter
@AllArgsConstructor
public class ExportFileResult {
    private String fileName;
    private MediaType mediaType;
    private ByteArrayInputStream stream;
}

