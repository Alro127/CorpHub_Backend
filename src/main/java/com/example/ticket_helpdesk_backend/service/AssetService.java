package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.AssetCategoryDto;
import com.example.ticket_helpdesk_backend.dto.AssetRequest;
import com.example.ticket_helpdesk_backend.dto.AssetResponse;
import com.example.ticket_helpdesk_backend.entity.Asset;
import com.example.ticket_helpdesk_backend.entity.AssetCategory;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AssetCategoryRepository;
import com.example.ticket_helpdesk_backend.repository.AssetRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssetService {
    final AssetRepository assetRepository;
     final ModelMapper modelMapper;
     final AssetCategoryRepository assetCategoryRepository;

    public AssetService(AssetRepository assetRepository,
                        ModelMapper modelMapper, AssetCategoryRepository assetCategoryRepository) {
        this.assetRepository = assetRepository;
        this.modelMapper = modelMapper;
        this.assetCategoryRepository = assetCategoryRepository;
    }

    public AssetResponse getAsset(UUID id) throws ResourceNotFoundException {
        return modelMapper.map(assetRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Asset not found")), AssetResponse.class);
    }

    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream().map(asset ->
                modelMapper.map(asset, AssetResponse.class)).collect(Collectors.toList());
    }

    public List<AssetCategoryDto> getCategories() {
        return assetCategoryRepository.findAll().stream().map((element) -> modelMapper.map(element, AssetCategoryDto.class)).collect(Collectors.toList());
    }

    public AssetCategory getAssetCategory(UUID id) {
        return assetCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("AssetCategory not found"));
    }

    public AssetResponse save(AssetRequest assetRequest) throws ResourceNotFoundException {
        Asset asset;
        if (assetRequest.getId() == null) {
            asset = modelMapper.map(assetRequest, Asset.class);
        }
        else {
            asset = assetRepository.findById(assetRequest.getId()).orElseThrow(
                    () -> new ResourceNotFoundException("Asset not found")
            );

            modelMapper.map(assetRequest, asset);
        }
        return modelMapper.map(assetRepository.save(asset), AssetResponse.class);
    }

    @Transactional
    public AssetResponse delete(UUID id) throws ResourceNotFoundException {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));

        assetRepository.delete(asset);

        return modelMapper.map(asset, AssetResponse.class);
    }
}
