package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class WorkflowTemplateRequest implements Serializable {
    @NotNull
    @Size(max = 200)
    String name;
    @NotNull
    @Size(max = 100)
    String targetEntity;
}