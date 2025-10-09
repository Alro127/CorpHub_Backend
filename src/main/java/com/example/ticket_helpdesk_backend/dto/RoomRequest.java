package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Room}
 */
@Value
public class RoomRequest implements Serializable {

    UUID id;

    @NotBlank(message = "Room name cannot be blank")
    @Size(max = 100, message = "Room name cannot exceed 100 characters")
    String name;

    @Size(max = 50, message = "Room type cannot exceed 50 characters")
    String type;

    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Area must be greater than 0")
    BigDecimal area;

    @NotBlank(message = "Status cannot be blank")
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    String status;

    @NotNull(message = "Department ID cannot be null")
    UUID departmentId;
}
