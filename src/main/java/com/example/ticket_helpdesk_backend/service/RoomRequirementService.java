package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.entity.RoomRequirementAsset;
import com.example.ticket_helpdesk_backend.repository.RoomRequirementAssetRepository;
import com.example.ticket_helpdesk_backend.repository.RoomRequirementRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomRequirementService {
    final RoomRequirementRepository roomRequirementRepository;
    final RoomRequirementAssetRepository roomRequirementAssetRepository;
    final AssetService assetService;

    public RoomRequirementService(RoomRequirementRepository roomRequirementRepository, RoomRequirementAssetRepository roomRequirementAssetRepository, AssetService assetService,
                                  ModelMapper modelMapper) {
        this.roomRequirementRepository = roomRequirementRepository;
        this.roomRequirementAssetRepository = roomRequirementAssetRepository;
        this.assetService = assetService;
    }

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
}
