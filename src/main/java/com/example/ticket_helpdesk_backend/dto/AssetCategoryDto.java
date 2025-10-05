package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.AssetCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.AssetCategory}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetCategoryDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @Size(max = 255)
    String description;

    public static AssetCategoryDto toAssetCategoryDto(AssetCategory assetCategory) {
        if (assetCategory == null) return null;
        return new AssetCategoryDto(
                assetCategory.getId(),
                assetCategory.getName(),
                assetCategory.getDescription()
        );
    }
}