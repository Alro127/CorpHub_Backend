package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.AssetCategory}
 */
@Data
public class AssetCategoryDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @Size(max = 255)
    String description;
}