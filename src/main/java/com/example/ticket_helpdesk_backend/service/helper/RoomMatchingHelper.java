package com.example.ticket_helpdesk_backend.service.helper;

import com.example.ticket_helpdesk_backend.consts.RoomRequirementStatus;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.entity.RoomRequirementAsset;
import com.example.ticket_helpdesk_backend.repository.RoomRequirementRepository;
import jdk.jfr.Category;
import lombok.AllArgsConstructor;
import lombok.experimental.Helper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class RoomMatchingHelper {
    private RoomRequirementRepository roomRequirementRepository;

    public boolean isAvailable(Room room, LocalDateTime start, LocalDateTime end) {
        // Giả sử room.getRoomRequirements() chứa lịch đã được duyệt
        for (RoomRequirement existing : roomRequirementRepository.findByRoom(room)) {
            if (existing.getStatus() == RoomRequirementStatus.ACCEPTED ||
                    existing.getStatus() == RoomRequirementStatus.PENDING) {
                // Nếu có trùng thời gian
                if (timesOverlap(start, end, existing.getStartTime(), existing.getEndTime())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean timesOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    public boolean hasCapacity(Room room, int requiredCapacity) {
        return room.getCapacity() != null && room.getCapacity() >= requiredCapacity;
    }

    public boolean hasRequiredAssets(Room room, List<RoomRequirementAsset> requiredAssets) {
        if (requiredAssets == null || requiredAssets.isEmpty()) return true;

        List<UUID> roomAssetCategoryIds = room.getAssets().stream()
                .map(asset -> asset.getCategory().getId())
                .distinct()
                .toList();

        for (RoomRequirementAsset reqAsset : requiredAssets) {
            UUID requiredCategoryId = reqAsset.getAssetCategory().getId();
            if (!roomAssetCategoryIds.contains(requiredCategoryId)) {
                return false;
            }
        }
        return true;
    }

    public double computeMatchScore(Room room, RoomRequirement req) {
        double score = 0.0;

        // Ưu tiên phòng có sức chứa gần với yêu cầu
        if (room.getCapacity() != null && req.getCapacity() != null) {
            int diff = Math.abs(room.getCapacity() - req.getCapacity());
            score += Math.max(0, 50 - diff); // càng gần càng điểm cao
        }

        // Ưu tiên phòng có đủ tài sản
        int requiredAssets = req.getRoomRequirementAssets().size();
        long matchedAssets = room.getAssets().stream()
                .map(a -> a.getCategory().getId())
                .filter(id -> req.getRoomRequirementAssets().stream()
                        .anyMatch(r -> r.getAssetCategory().getId().equals(id)))
                .count();
        if (requiredAssets > 0) {
            score += (double) matchedAssets / requiredAssets * 50.0;
        } else {
            score += 20; // bonus nếu không yêu cầu gì
        }

        return score; // tổng tối đa 100
    }


}
