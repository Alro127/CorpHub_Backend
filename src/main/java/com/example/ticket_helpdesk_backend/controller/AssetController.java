package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.AssetCategoryDto;
import com.example.ticket_helpdesk_backend.dto.AssetRequest;
import com.example.ticket_helpdesk_backend.dto.AssetResponse;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Lấy tất cả asset
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getAllAssets() {
        List<AssetResponse> assets = assetService.getAllAssets();
        ApiResponse<List<AssetResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched all assets successfully",
                LocalDateTime.now(),
                assets
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

    // Tạo mới hoặc cập nhật asset
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> saveAsset(@RequestBody AssetRequest assetRequest) throws ResourceNotFoundException {
        AssetResponse savedAsset = assetService.save(assetRequest);
        ApiResponse<AssetResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                assetRequest.getId() == null ? "Asset created successfully" : "Asset updated successfully",
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
}
