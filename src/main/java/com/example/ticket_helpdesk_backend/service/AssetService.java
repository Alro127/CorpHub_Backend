package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AssetStatus;
import com.example.ticket_helpdesk_backend.dto.AssetCategoryDto;
import com.example.ticket_helpdesk_backend.dto.AssetRequest;
import com.example.ticket_helpdesk_backend.dto.AssetResponse;
import com.example.ticket_helpdesk_backend.entity.Asset;
import com.example.ticket_helpdesk_backend.entity.AssetCategory;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AssetCategoryRepository;
import com.example.ticket_helpdesk_backend.repository.AssetRepository;
import com.example.ticket_helpdesk_backend.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.ticket_helpdesk_backend.specification.AssetSpecifications.*;

@Service
@AllArgsConstructor
public class AssetService {
    final AssetRepository assetRepository;
    final ModelMapper modelMapper;
    final AssetCategoryRepository assetCategoryRepository;
    final RoomRepository roomRepository;

    public AssetResponse getAsset(UUID id) throws ResourceNotFoundException {
        return modelMapper.map(assetRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Asset not found")), AssetResponse.class);
    }

    public Page<AssetResponse> getAllAssets(int page, int size, String keywords, UUID categoryId, AssetStatus status) {
        Specification<Asset> spec = Specification.where(hasKeyword(keywords)
                        .and(hasCategory(categoryId)).and(hasStatus(status)));
        Pageable pageable = PageRequest.of(page, size);
        return assetRepository.findAll(spec, pageable).map(asset -> modelMapper.map(asset, AssetResponse.class));
    }

    public List<AssetCategoryDto> getCategories() {
        return assetCategoryRepository.findAll().stream().map((element) -> modelMapper.map(element, AssetCategoryDto.class)).collect(Collectors.toList());
    }

    public AssetCategory getAssetCategory(UUID id) {
        return assetCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("AssetCategory not found"));
    }

    public AssetResponse create(AssetRequest assetRequest) throws ResourceNotFoundException {
        Asset asset = modelMapper.map(assetRequest, Asset.class);
        return modelMapper.map(assetRepository.save(asset), AssetResponse.class);
    }

    @Transactional
    public AssetResponse update(AssetRequest dto) throws ResourceNotFoundException {
        Asset asset = assetRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        // Map thủ công các field đơn giản
        asset.setName(dto.getName());
        asset.setCode(dto.getCode());
        asset.setStatus(dto.getStatus());
        asset.setValue(dto.getValue());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setWarranty(dto.getWarranty());

        // ✅ Lấy room từ DB, không tạo mới
        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            asset.setRoom(room);
        } else {
            asset.setRoom(null);
        }

        // ✅ Lấy category từ DB
        if (dto.getCategoryId() != null) {
            AssetCategory category = assetCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            asset.setCategory(category);
        } else {
            asset.setCategory(null);
        }

        Asset saved = assetRepository.save(asset);
        return modelMapper.map(saved, AssetResponse.class);
    }


    @Transactional
    public AssetResponse delete(UUID id) throws ResourceNotFoundException {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));

        assetRepository.delete(asset);

        return modelMapper.map(asset, AssetResponse.class);
    }

    @Transactional
    public boolean removeAssetFromRoom(UUID assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (asset.getRoom() == null) {
            return false;
        }

        asset.setRoom(null);
        assetRepository.save(asset);

        return true;
    }

    public Map<AssetStatus, Integer> getAssetCounts() {
        Map<AssetStatus, Integer> result = new java.util.HashMap<>();

        for (AssetStatus assetStatus : AssetStatus.values()) {
            int count = assetRepository.countByStatus(assetStatus);
            result.put(assetStatus, count);
        }

        return result;
    }
}
