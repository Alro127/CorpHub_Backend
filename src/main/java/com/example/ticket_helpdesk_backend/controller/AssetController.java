package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.AssetStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AssetService;
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
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Lấy tất cả asset
    @GetMapping
    public ResponseEntity<?> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) AssetStatus status) {
        Page<AssetResponse> pageData = assetService.getAllAssets(
                page, size, keywords, categoryId, status
        );

        Map<AssetStatus, Integer> assetCounts = assetService.getAssetCounts();
        ApiResponse<List<AssetResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched all assets successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast(),
                        "assetCounts", assetCounts
                )
        );
        return ResponseEntity.ok(response);
    }

    // Lấy asset theo id
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> getAsset(@PathVariable UUID id) throws ResourceNotFoundException {
        AssetResponse asset = assetService.getAsset(id);
        ApiResponse<AssetResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched asset successfully",
                LocalDateTime.now(),
                asset
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<AssetCategoryDto> categories = assetService.getCategories();
        ApiResponse<List<AssetCategoryDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched asset categories successfully",
                LocalDateTime.now(),
                categories
        );
        return ResponseEntity.ok(response);
    }

    // Tạo mới
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(@RequestBody AssetRequest assetRequest) throws ResourceNotFoundException {
        AssetResponse savedAsset = assetService.create(assetRequest);
        ApiResponse<AssetResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                 "Asset created successfully",
                LocalDateTime.now(),
                savedAsset
        );
        return ResponseEntity.ok(response);
    }

    // Cập nhật
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(@RequestBody AssetRequest assetRequest) throws ResourceNotFoundException {
        AssetResponse savedAsset = assetService.update(assetRequest);
        ApiResponse<AssetResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Asset updated successfully",
                LocalDateTime.now(),
                savedAsset
        );
        return ResponseEntity.ok(response);
    }

    // Xóa asset theo id
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> deleteAsset(@PathVariable UUID id) throws ResourceNotFoundException {
        AssetResponse deletedAsset = assetService.delete(id);
        ApiResponse<AssetResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Asset deleted successfully",
                LocalDateTime.now(),
                deletedAsset
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping("/remove-from-room")
    public ResponseEntity<?> removeAssetFromRoom(@RequestParam UUID assetId) {
        boolean success = assetService.removeAssetFromRoom(assetId);

        ApiResponse<Boolean> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                success ? "Asset removed successfully" : "Asset is not assigned to any room",
                LocalDateTime.now(),
                success
        );
        return ResponseEntity.ok(response);
    }
}
