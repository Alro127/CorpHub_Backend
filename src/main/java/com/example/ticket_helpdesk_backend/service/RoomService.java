package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.AssetResponse;
import com.example.ticket_helpdesk_backend.dto.RoomRequest;
import com.example.ticket_helpdesk_backend.dto.RoomResponse;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.RoomRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    private RoomResponse mapToResponse(Room room) {
        RoomResponse response = modelMapper.map(room, RoomResponse.class);
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
                       ModelMapper modelMapper) {
        this.roomRepository = roomRepository;
        this.modelMapper = modelMapper;
    }

    public RoomResponse getRoom(UUID id) throws ResourceNotFoundException {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

}
