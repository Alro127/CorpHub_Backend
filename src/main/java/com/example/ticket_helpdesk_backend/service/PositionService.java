package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.PositionRequest;
import com.example.ticket_helpdesk_backend.dto.PositionResponse;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.Position;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.PositionRepository;
import com.example.ticket_helpdesk_backend.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public PositionResponse createPosition(UUID departmentId, PositionRequest request) throws ResourceNotFoundException {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Position pos = new Position();
        pos.setDepartment(department);
        pos.setName(request.getName());
        pos.setCode(request.getCode());
        pos.setDescription(request.getDescription());
        pos.setLevelOrder(request.getLevelOrder());

        positionRepository.save(pos);
        return PositionResponse.fromEntity(pos);
    }

    @Transactional
    public PositionResponse updatePosition(UUID id, PositionRequest request) throws ResourceNotFoundException {
        Position pos = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        pos.setName(request.getName());
        pos.setCode(request.getCode());
        pos.setDescription(request.getDescription());
        pos.setLevelOrder(request.getLevelOrder());

        positionRepository.save(pos);
        return PositionResponse.fromEntity(pos);
    }

    @Transactional
    public void deletePosition(UUID id) throws ResourceNotFoundException {
        Position pos = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        positionRepository.delete(pos);
    }

    @Transactional
    public List<PositionResponse> getPositionsByDepartment(UUID departmentId) {
        return positionRepository.findByDepartmentIdOrderByLevelOrderAsc(departmentId)
                .stream()
                .map(PositionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reorderPositions(UUID departmentId, List<UUID> orderedIds) {
        List<Position> positions = positionRepository.findByDepartmentIdOrderByLevelOrderAsc(departmentId);

        Map<UUID, Position> map = positions.stream()
                .collect(Collectors.toMap(Position::getId, p -> p));

        int index = 1;
        for (UUID posId : orderedIds) {
            Position p = map.get(posId);
            if (p != null) {
                p.setLevelOrder(index++);
                positionRepository.save(p);
            }
        }
    }
}
