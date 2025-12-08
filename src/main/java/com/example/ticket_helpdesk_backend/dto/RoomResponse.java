package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.RoomStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Room}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    RoomTypeDto type;
    Integer capacity;
    BigDecimal area;
    @NotNull
    @Size(max = 20)
    RoomStatus status;
    DepartmentDto department;
    List<AssetResponse> assets;
}