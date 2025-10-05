package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
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
public class RoomRequirementDto {
    private UUID id;
    private int capacity;
    private List<UUID> assetCategories;
    private LocalDateTime start;
    private LocalDateTime end;
    private UUID roomId;

    static public RoomRequirementDto toRoomRequirementDto(RoomRequirement roomRequirement) {
        if (roomRequirement == null) {
            return null;
        }
        RoomRequirementDto dto = new RoomRequirementDto();
        dto.setId(roomRequirement.getId());
        dto.setCapacity(roomRequirement.getCapacity());
        dto.setStart(roomRequirement.getStartTime());
        dto.setEnd(roomRequirement.getEndTime());
        if (roomRequirement.getRoom() != null) dto.setRoomId(roomRequirement.getRoom().getId());
        else dto.setRoomId(null);

        dto.setAssetCategories(roomRequirement.getRoomRequirementAssets().stream().map(roomRequirementAsset -> roomRequirementAsset.getAssetCategory().getId()).collect(Collectors.toList()));
        return dto;
    }
}
