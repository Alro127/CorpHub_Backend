package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.AssetStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Asset}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @NotNull
    @Size(max = 50)
    String code;
    AssetCategoryDto category;
    @NotNull
    @Size(max = 20)
    AssetStatus status;
    BigDecimal value;
    LocalDate purchaseDate;
    LocalDate warranty;
    UUID roomId;
    String roomName;
}