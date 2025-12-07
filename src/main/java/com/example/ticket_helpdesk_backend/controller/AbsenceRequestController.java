package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.AbsenceRequestStatus;
import com.example.ticket_helpdesk_backend.consts.WorkflowActionType;
import com.example.ticket_helpdesk_backend.consts.WorkflowStatus;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqResponse;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqRequest;
import com.example.ticket_helpdesk_backend.dto.ApproveRejectRequest;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AbsenceRequestService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/absence/requests")
public class AbsenceRequestController {

    private final AbsenceRequestService absenceRequestService;
    private final JwtUtil jwtUtil;

    /* ----------------------------------------------------
     * 1️⃣ Lấy tất cả (dành cho quản lý duyệt)
     * ---------------------------------------------------- */
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAbsenceRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AbsenceReqResponse> pageData = absenceRequestService.getAll(page, size);

        ApiResponse<List<AbsenceReqResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetch all absence requests successfully",
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

    /* ----------------------------------------------------
     * 2️⃣ Lấy danh sách đơn nghỉ của nhân viên hiện tại
     * ---------------------------------------------------- */
    @GetMapping("/my")
    public ResponseEntity<?> getMyAbsenceRequests(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        Page<AbsenceReqResponse> pageData = absenceRequestService.getByUser(userId, page, size);

        ApiResponse<List<AbsenceReqResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetch my absence requests successfully",
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

    @GetMapping("/my-approvals")
    public ResponseEntity<?> getMyApprovals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) WorkflowActionType action
    ) {
        UUID userId = jwtUtil.getUserId(authHeader.substring(7));

        Page<AbsenceReqResponse> pageData =
                absenceRequestService.getAllApprovals(userId, page, size, action);

        ApiResponse<List<AbsenceReqResponse>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Fetched all approvals related to user",
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


    /* ----------------------------------------------------
     * 3️⃣ Tạo đơn nghỉ phép
     * ---------------------------------------------------- */
    @PostMapping
    public ResponseEntity<?> createAbsenceRequest(@RequestHeader("Authorization") String authHeader, @RequestBody AbsenceReqRequest request) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        AbsenceReqResponse created = absenceRequestService.create(userId, request);
        ApiResponse<AbsenceReqResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Create absence request successfully",
                LocalDateTime.now(),
                created
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAbsenceRequest(@RequestHeader("Authorization") String authHeader, @PathVariable UUID id, @RequestBody AbsenceReqRequest request) throws ResourceNotFoundException, AuthException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        AbsenceReqResponse updated = absenceRequestService.update(userId, id, request);

        ApiResponse<AbsenceReqResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Create absence request successfully",
                LocalDateTime.now(),
                updated
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /* ----------------------------------------------------
     * 4️⃣ Xem chi tiết đơn nghỉ
     * ---------------------------------------------------- */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAbsenceRequestById(@PathVariable UUID id) {
        AbsenceReqResponse detail = absenceRequestService.getById(id);

        ApiResponse<AbsenceReqResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetch absence request detail successfully",
                LocalDateTime.now(),
                detail
        );

        return ResponseEntity.ok(response);
    }

    /* ----------------------------------------------------
     * 5️⃣ Xóa đơn nghỉ (chỉ cho phép khi PENDING)
     * ---------------------------------------------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbsenceRequest(@RequestHeader("Authorization") String authHeader, @PathVariable UUID id) throws ResourceNotFoundException, AuthException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        absenceRequestService.deleteByUser(id, userId);

        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Delete absence request successfully",
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.ok(response);
    }

    /* ----------------------------------------------------
     * 6️⃣ Danh sách request mà người dùng hiện tại cần duyệt
     * ---------------------------------------------------- */
    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingForApprover(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        Page<AbsenceReqResponse> pageData =
                absenceRequestService.getPendingForApprover(userId, page, size);

        ApiResponse<List<AbsenceReqResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetch pending approval requests successfully",
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



    @PutMapping("/{instanceId}/approval")
    public ResponseEntity<?> approveOrReject(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID instanceId,
            @RequestBody ApproveRejectRequest req
    ) {
        String token = authHeader.substring(7);
        UUID actorId = jwtUtil.getUserId(token);

        AbsenceReqResponse updated = absenceRequestService.approveOrReject(
                instanceId,
                actorId,
                req.isApprove(),
                req.getComment()
        );

        ApiResponse<AbsenceReqResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                req.isApprove()
                        ? "Approved absence request successfully"
                        : "Rejected absence request",
                LocalDateTime.now(),
                updated
        );

        return ResponseEntity.ok(response);
    }

}
