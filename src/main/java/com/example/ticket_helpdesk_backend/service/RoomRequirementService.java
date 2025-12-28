package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.RoomRequirementStatus;
import com.example.ticket_helpdesk_backend.consts.RoomStatus;
import com.example.ticket_helpdesk_backend.dto.RoomAllocationSuggestion;
import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.entity.RoomRequirementAsset;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.service.helper.RoomMatchScore;
import com.example.ticket_helpdesk_backend.service.helper.RoomMatchingHelper;
import com.example.ticket_helpdesk_backend.specification.RoomRequirementSpecifications;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomRequirementService {
    final RoomRequirementRepository roomRequirementRepository;
    final RoomRequirementAssetRepository roomRequirementAssetRepository;
    final AssetService assetService;
    private final RoomRepository roomRepository;
    private final MeetingRepository meetingRepository;
    ModelMapper modelMapper;
    private final RoomMatchingHelper matchingHelper;

    public RoomRequirement getRoomRequirementById(UUID id) {
        return roomRequirementRepository.findById(id).orElse(null);
    }

    public Page<RoomRequirementDto> getAllRoomRequirements(int page, int size, RoomRequirementStatus status) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<RoomRequirement> spec = Specification
                .where(RoomRequirementSpecifications.hasStatus(status));

        Page<RoomRequirement> roomRequirements = roomRequirementRepository.findAll(spec, pageable);
        return roomRequirements.map(RoomRequirementDto::toRoomRequirementDto);
    }

    public RoomRequirement getRoomRequirementMeetingId(UUID id) {
        return roomRequirementRepository.findByMeetingId(id);
    }

    @Transactional
    public void saveRoomRequirement(RoomRequirementDto req, Meeting meeting) {
        RoomRequirement roomRequirement;

        // Tạo mới nếu id null
        if (req.getId() == null) {
            roomRequirement = new RoomRequirement();
        } else {
            // Update nếu id tồn tại
            roomRequirement = roomRequirementRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Room requirement not found"));
        }

        // Cập nhật field
        roomRequirement.setCapacity(req.getCapacity());
        roomRequirement.setStartTime(req.getStart());
        roomRequirement.setEndTime(req.getEnd());
        roomRequirement.setMeeting(meeting);
        roomRequirement.setStatus(RoomRequirementStatus.PENDING);

        // Lưu trước để đảm bảo có id
        RoomRequirement saved = roomRequirementRepository.save(roomRequirement);

        // Đồng bộ assets
        roomRequirement.getRoomRequirementAssets().clear();
        for (UUID assetCategoryId : req.getAssetCategories()) {
            RoomRequirementAsset asset = new RoomRequirementAsset();
            asset.setRoomRequirement(roomRequirement);
            asset.setAssetCategory(assetService.getAssetCategory(assetCategoryId));
            roomRequirement.getRoomRequirementAssets().add(asset);
        }
        roomRequirementRepository.save(roomRequirement);

    }

    @Transactional
    public void deleteRoomRequirementByMeetingId(UUID id) {
        RoomRequirement roomRequirement = roomRequirementRepository.findByMeetingId(id);
        if (roomRequirement == null) {
            return;
        }
        roomRequirementRepository.delete(roomRequirement);
    }

    @Transactional
    public Boolean setUpRoom(UUID id, UUID roomId) {
        try {

            RoomRequirement roomRequirement = roomRequirementRepository.findById(id).orElseThrow(() -> new RuntimeException("Room requirement not found"));

            if (roomRequirement.getStatus() == RoomRequirementStatus.CLOSED) {
                return false;
            }

            Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
            roomRequirement.setRoom(room);
            roomRequirement.setStatus(RoomRequirementStatus.ACCEPTED);
            roomRequirementRepository.save(roomRequirement);

            room.setStatus(RoomStatus.RESERVED);

            Meeting meeting = roomRequirement.getMeeting();
            meeting.setLocation(room.getName());
            meetingRepository.save(meeting);
            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public List<RoomRequirementDto> getRoomRequirementsByRoomAndDate(UUID roomId, LocalDate date)
            throws ResourceNotFoundException {

        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        Specification<RoomRequirement> spec = Specification
                .where(RoomRequirementSpecifications.isInDate(date));

        if (roomId != null) {
            spec = spec.and(RoomRequirementSpecifications.hasRoomId(roomId));
        }

        List<RoomRequirement> requirements = roomRequirementRepository.findAll(spec);

        return requirements.stream().map(RoomRequirementDto::toRoomRequirementDto).collect(Collectors.toList());
    }

    public List<RoomAllocationSuggestion> suggestAllocations(List<UUID> requirementIds) {
        // Lấy danh sách các yêu cầu
        List<RoomRequirement> requirements = roomRequirementRepository.findAllById(requirementIds);
        List<Room> allRooms = roomRepository.findAll();

        List<RoomAllocationSuggestion> results = new ArrayList<>();

        for (RoomRequirement req : requirements) {
            // Lọc các phòng đủ điều kiện
            List<Room> suitable = allRooms.stream()
                    .filter(r -> matchingHelper.isAvailable(r, req.getStartTime(), req.getEndTime()))
                    .filter(r -> matchingHelper.hasCapacity(r, req.getCapacity()))
                    .filter(r -> matchingHelper.hasRequiredAssets(r, req.getRoomRequirementAssets()))
                    .filter(r -> r.getStatus().equals(RoomStatus.AVAILABLE))
                    .toList();

            // Xếp hạng độ phù hợp
            List<RoomMatchScore> scored = suitable.stream()
                    .map(r -> new RoomMatchScore(r, matchingHelper.computeMatchScore(r, req)))
                    .sorted(Comparator.comparingDouble(RoomMatchScore::getScore).reversed())
                    .toList();

            // Lấy phòng tốt nhất (hoặc top 3)
            if (!scored.isEmpty()) {
                RoomMatchScore best = scored.getFirst();
                results.add(new RoomAllocationSuggestion(
                        req.getId(),
                        best.getRoom().getId(),
                        best.getRoom().getName(),
                        best.getScore()
                ));
            } else {
                // Không tìm thấy phòng phù hợp
                results.add(new RoomAllocationSuggestion(
                        req.getId(),
                        null,
                        null,
                        0.0
                ));
            }
        }

        return results;
    }

}
