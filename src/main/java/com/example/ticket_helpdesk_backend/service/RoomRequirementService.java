package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.RoomRequirementStatus;
import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.entity.RoomRequirementAsset;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
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

    public RoomRequirement getRoomRequirementById(UUID id) {
        return roomRequirementRepository.findById(id).orElse(null);
    }

    public Page<RoomRequirementDto> getAllRoomRequirements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomRequirement> roomRequirements = roomRequirementRepository.findAll(pageable);
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
        roomRequirementAssetRepository.deleteByRoomRequirementId(saved.getId());

        for (UUID assetCategoryId : req.getAssetCategories()) {
            RoomRequirementAsset roomRequirementAsset = new RoomRequirementAsset();
            roomRequirementAsset.setRoomRequirement(saved);
            roomRequirementAsset.setAssetCategory(assetService.getAssetCategory(assetCategoryId));

            roomRequirementAssetRepository.save(roomRequirementAsset);
        }
    }

    @Transactional
    public void deleteRoomRequirementByMeetingId(UUID id) {
        RoomRequirement roomRequirement = roomRequirementRepository.findByMeetingId(id);
        if (roomRequirement == null) {
            return;
        }
        roomRequirementRepository.delete(roomRequirement);
    }

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
}
