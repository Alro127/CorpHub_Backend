package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.CompetencyLevelDto;
import com.example.ticket_helpdesk_backend.dto.CompetencyTypeDto;
import com.example.ticket_helpdesk_backend.entity.CompetencyLevel;
import com.example.ticket_helpdesk_backend.entity.CompetencyType;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.CompetencyTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetencyTypeService {

    private final CompetencyTypeRepository repository;

    public List<CompetencyTypeDto> getAll() {
        return repository.findAll().stream()
                .map(CompetencyTypeDto::fromEntity)
                .toList();
    }

    public CompetencyTypeDto getById(UUID id) throws ResourceNotFoundException {
        CompetencyType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyType not found with id " + id));
        return CompetencyTypeDto.fromEntity(entity);
    }

    public CompetencyTypeDto create(CompetencyTypeDto dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("CompetencyType with code already exists");
        }

        CompetencyType entity = new CompetencyType();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());

        if (dto.getLevels() != null) {
            List<CompetencyLevel> levels = dto.getLevels().stream()
                    .map(l -> {
                        CompetencyLevel level = l.toEntity();
                        level.setType(entity);
                        return level;
                    })
                    .collect(Collectors.toList());
            entity.setLevels(levels);
        }

        CompetencyType saved = repository.save(entity);
        return CompetencyTypeDto.fromEntity(saved);
    }

    public CompetencyTypeDto update(UUID id, CompetencyTypeDto dto) throws ResourceNotFoundException {
        CompetencyType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyType not found with id " + id));

        entity.setCode(dto.getCode());
        entity.setName(dto.getName());

        // Đồng bộ danh sách level
        syncLevels(entity, dto.getLevels());

        CompetencyType updated = repository.save(entity);
        return CompetencyTypeDto.fromEntity(updated);
    }

    private void syncLevels(CompetencyType entity, List<CompetencyLevelDto> newLevels) {
        if (newLevels == null) {
            entity.getLevels().clear();
            return;
        }

        // map hiện tại
        Map<UUID, CompetencyLevel> existingMap = entity.getLevels().stream()
                .collect(Collectors.toMap(CompetencyLevel::getId, l -> l));

        // Cập nhật hoặc thêm mới
        List<CompetencyLevel> merged = new ArrayList<>();
        for (CompetencyLevelDto dto : newLevels) {
            CompetencyLevel level = dto.getId() != null ? existingMap.get(dto.getId()) : null;
            if (level == null) {
                level = dto.toEntity();
                level.setType(entity);
            } else {
                level.setName(dto.getName());
                level.setValueScale(dto.getValueScale());
            }
            merged.add(level);
        }

        // Xóa level cũ không còn trong danh sách
        entity.getLevels().clear();
        entity.getLevels().addAll(merged);
    }

    public void delete(UUID id) throws ResourceNotFoundException {
        CompetencyType entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyType not found with id " + id));
        repository.delete(entity);
    }
}
