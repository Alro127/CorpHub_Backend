package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.CompetencyLevelDto;
import com.example.ticket_helpdesk_backend.entity.CompetencyLevel;
import com.example.ticket_helpdesk_backend.entity.CompetencyType;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.CompetencyLevelRepository;
import com.example.ticket_helpdesk_backend.repository.CompetencyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetencyLevelService {

    private final CompetencyLevelRepository levelRepository;
    private final CompetencyTypeRepository typeRepository;

    public List<CompetencyLevelDto> getAll() {
        return levelRepository.findAll().stream()
                .map(CompetencyLevelDto::fromEntity)
                .toList();
    }

    public List<CompetencyLevelDto> getByType(UUID typeId) {
        return levelRepository.findByTypeId(typeId).stream()
                .map(CompetencyLevelDto::fromEntity)
                .toList();
    }

    public CompetencyLevelDto getById(UUID id) throws ResourceNotFoundException {
        CompetencyLevel entity = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyLevel not found with id " + id));
        return CompetencyLevelDto.fromEntity(entity);
    }

    public CompetencyLevelDto create(UUID typeId, CompetencyLevelDto dto) throws ResourceNotFoundException {
        CompetencyType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyType not found with id " + typeId));

        CompetencyLevel level = dto.toEntity();
        level.setType(type);
        CompetencyLevel saved = levelRepository.save(level);
        return CompetencyLevelDto.fromEntity(saved);
    }

    public CompetencyLevelDto update(UUID id, CompetencyLevelDto dto) throws ResourceNotFoundException {
        CompetencyLevel entity = levelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompetencyLevel not found with id " + id));

        entity.setName(dto.getName());
        entity.setValueScale(dto.getValueScale());
        CompetencyLevel updated = levelRepository.save(entity);
        return CompetencyLevelDto.fromEntity(updated);
    }

    public void delete(UUID id) throws ResourceNotFoundException {
        if (!levelRepository.existsById(id)) {
            throw new ResourceNotFoundException("CompetencyLevel not found with id " + id);
        }
        levelRepository.deleteById(id);
    }
}
