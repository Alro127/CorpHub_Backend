package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.RoomStatus;
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

    @NotNull(message = "Room type cannot be null")
    UUID typeId;

    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Area must be greater than 0")
    BigDecimal area;

    @NotNull(message = "Status must not be null")
    RoomStatus status;

    @NotNull(message = "Department ID cannot be null")
    UUID departmentId;
}
