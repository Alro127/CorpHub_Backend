package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssignAssetsRequest {
    @NotEmpty
    private List<UUID> assetIds;

    @NotNull
    private UUID roomId;
}
