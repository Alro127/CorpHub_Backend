package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.validation.ValidTimeRange;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidTimeRange
public class RoomRequirementDto {

    private UUID id;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    private List<UUID> assetCategories;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime end;

    private UUID roomId;

    private String roomName;

    private String status;

    public static RoomRequirementDto toRoomRequirementDto(RoomRequirement roomRequirement) {
        if (roomRequirement == null) {
            return null;
        }
        RoomRequirementDto dto = new RoomRequirementDto();
        dto.setId(roomRequirement.getId());
        dto.setCapacity(roomRequirement.getCapacity());
        dto.setStart(roomRequirement.getStartTime());
        dto.setEnd(roomRequirement.getEndTime());
        dto.setRoomId(roomRequirement.getRoom() != null ? roomRequirement.getRoom().getId() : null);
        dto.setRoomName(roomRequirement.getRoom() != null ? roomRequirement.getRoom().getName() : null);
        dto.setStatus(roomRequirement.getStatus());
        dto.setAssetCategories(
                roomRequirement.getRoomRequirementAssets().stream()
                        .map(a -> a.getAssetCategory().getId())
                        .collect(Collectors.toList())
        );
        return dto;
    }
}
