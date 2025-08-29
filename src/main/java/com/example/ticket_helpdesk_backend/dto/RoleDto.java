package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Role}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 50)
    String name;
}