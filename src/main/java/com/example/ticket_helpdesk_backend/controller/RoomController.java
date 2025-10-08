package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.RoomRequest;
import com.example.ticket_helpdesk_backend.dto.RoomResponse;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.RoomService;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Lấy danh sách tất cả phòng
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RoomResponse> pageData = roomService.getAllRooms(page, size);
        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Rooms fetched successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );
        return ResponseEntity.ok(response);
    }

    // Lấy thông tin 1 phòng theo id
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable UUID id) throws ResourceNotFoundException {
        RoomResponse room = roomService.getRoom(id);
        ApiResponse<RoomResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched room successfully",
                LocalDateTime.now(),
                room
        );
        return ResponseEntity.ok(response);
    }

    // Tạo hoặc cập nhật phòng
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> saveRoom(@RequestBody RoomRequest roomRequest) throws ResourceNotFoundException {
        RoomResponse savedRoom = roomService.save(roomRequest);
        ApiResponse<RoomResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                roomRequest.getId() == null ? "Room created successfully" : "Room updated successfully",
                LocalDateTime.now(),
                savedRoom
        );
        return ResponseEntity.ok(response);
    }

    // Xóa phòng theo id
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> deleteRoom(@PathVariable UUID id) throws ResourceNotFoundException {
        RoomResponse deletedRoom = roomService.delete(id);
        ApiResponse<RoomResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Room deleted successfully",
                LocalDateTime.now(),
                deletedRoom
        );
        return ResponseEntity.ok(response);
    }
}
