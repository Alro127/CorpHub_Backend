package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Department}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto implements Serializable {
    UUID id;
    @Size(max = 100)
    String name;
    String description;
}