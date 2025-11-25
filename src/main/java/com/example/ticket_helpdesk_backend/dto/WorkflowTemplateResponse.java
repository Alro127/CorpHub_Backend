package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class WorkflowTemplateResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 200)
    String name;
    @NotNull
    @Size(max = 100)
    String targetEntity;
    @NotNull
    LocalDateTime createdAt;
    @NotNull
    LocalDateTime updatedAt;
}