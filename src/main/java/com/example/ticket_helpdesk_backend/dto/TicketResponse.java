package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String title;
    String description;
    @NotNull
    @Size(max = 10)
    String priority;
    @NotNull
    @Enumerated(EnumType.STRING)
    TicketStatus status;
    @NotNull
    TicketCategoryDto category;
    @NotNull
    NameInfoDto requester;
    NameInfoDto assignedTo;
    @NotNull
    DepartmentBasicInfoDto department;
    @NotNull
    LocalDateTime createdAt;
    @NotNull
    LocalDateTime updatedAt;
    LocalDateTime assignedAt;
    Instant resolvedAt;
    @NotNull
    Boolean active;

}