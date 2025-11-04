package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.RoomType}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 50)
    String code;
    @NotNull
    @Size(max = 100)
    String name;
    String description;
}