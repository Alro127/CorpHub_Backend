package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.Asset;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AssetRepository;
import com.example.ticket_helpdesk_backend.repository.RoomRepository;
import com.example.ticket_helpdesk_backend.repository.RoomRequirementRepository;
import com.example.ticket_helpdesk_backend.specification.RoomRequirementSpecifications;
import com.example.ticket_helpdesk_backend.specification.RoomSpecifications;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.ticket_helpdesk_backend.specification.RoomSpecifications.*;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final RoomRequirementRepository roomRequirementRepository;
    private final AssetRepository assetRepository;

    private RoomResponse mapToResponse(Room room) {
        RoomResponse response = modelMapper.map(room, RoomResponse.class);
        response.setType(modelMapper.map(room.getType(), RoomTypeDto.class));
        if (room.getAssets() != null) {
            response.setAssets(
                    room.getAssets().stream()
                            .map(asset -> modelMapper.map(asset, AssetResponse.class))
                            .collect(Collectors.toList())
            );
        }
        return response;
    }

    public RoomService(RoomRepository roomRepository,
                       ModelMapper modelMapper, RoomRequirementRepository roomRequirementRepository, AssetRepository assetRepository) {
        this.roomRepository = roomRepository;
        this.modelMapper = modelMapper;
        this.roomRequirementRepository = roomRequirementRepository;
        this.assetRepository = assetRepository;
    }

    public RoomResponse getRoom(UUID id) throws ResourceNotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    public Page<RoomResponse> getAllRooms(int page, int size, String keywords, UUID typeId, UUID departmentId, Integer minCapacity, BigDecimal minArea, String status) {
        System.out.println(departmentId);

        Specification<Room> spec = Specification.where(hasName(keywords))
                .and(hasType(typeId))
                .and(belongsToDepartment(departmentId))
                .and(hasMinCapacity(minCapacity))
                .and(hasMinArea(minArea))
                .and(hasStatus(status));

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> rooms = roomRepository.findAll(spec, pageable);
        return rooms.map(this::mapToResponse);
    }

    public RoomResponse save(RoomRequest roomRequest) throws ResourceNotFoundException {
        Room room;

        if (roomRequest.getId() == null) {
            room = modelMapper.map(roomRequest, Room.class);
        } else {
            room = roomRepository.findById(roomRequest.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + roomRequest.getId()));

            modelMapper.map(roomRequest, room);
        }

        Room savedRoom = roomRepository.save(room);
        return modelMapper.map(savedRoom, RoomResponse.class);
    }

    @Transactional
    public RoomResponse delete(UUID id) throws ResourceNotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));

        // gỡ liên kết assets -> set room_id = null
        if (room.getAssets() != null) {
            room.getAssets().forEach(asset -> asset.setRoom(null));
        }

        roomRepository.delete(room);

        return modelMapper.map(room, RoomResponse.class);
    }

    private boolean isRoomAvailable(UUID roomId, LocalDateTime start, LocalDateTime end) {
        return !roomRequirementRepository.exists(
                RoomRequirementSpecifications.roomHasConflict(roomId, start, end)
        );
    }

    public List<RoomResponse> getSuitableRoom(UUID id) throws ResourceNotFoundException {

        RoomRequirement roomRequirement = roomRequirementRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Room Requirement not found")
        );

        LocalDateTime start = roomRequirement.getStartTime();
        LocalDateTime end = roomRequirement.getEndTime();

        // 1️⃣ Lấy tất cả phòng có capacity >= yêu cầu và status = "AVAILABLE"
        List<Room> candidateRooms = roomRepository.findAll(
                Specification.where(RoomSpecifications.hasMinCapacity(roomRequirement.getCapacity()))
                        .and(RoomSpecifications.hasStatus("AVAILABLE"))
        );

        // 2️⃣ Lọc ra các phòng có đủ tài sản
        if (roomRequirement.getRoomRequirementAssets() != null && !roomRequirement.getRoomRequirementAssets().isEmpty()) {
            candidateRooms = candidateRooms.stream()
                    .filter(room -> {
                        Set<UUID> roomAssetCategoryIds = room.getAssets().stream()
                                .map(asset -> asset.getCategory().getId())
                                .collect(Collectors.toSet());

                        Set<UUID> requiredCategoryIds = roomRequirement.getRoomRequirementAssets().stream()
                                .map(reqAsset -> reqAsset.getAssetCategory().getId())
                                .collect(Collectors.toSet());

                        return roomAssetCategoryIds.containsAll(requiredCategoryIds);
                    })
                    .toList();
        }


        // 3️⃣ Lọc ra các phòng không bị trùng thời gian đặt
        List<Room> availableRooms = candidateRooms.stream()
                .filter(room -> isRoomAvailable(room.getId(), start, end))
                .toList();

        // 4️⃣ Map sang DTO phản hồi
        return availableRooms.stream().map((element) -> modelMapper.map(element, RoomResponse.class)).collect(Collectors.toList());
    }

    public int assignAssets(UUID roomId, List<UUID> assetIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<Asset> assets = assetRepository.findAllById(assetIds);
        assets.forEach(a -> a.setRoom(room));

        List<Asset> savedAssets = assetRepository.saveAll(assets);
        return savedAssets.size();
    }



}
