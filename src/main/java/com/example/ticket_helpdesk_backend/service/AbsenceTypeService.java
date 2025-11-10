package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.AbsenceTypeRequest;
import com.example.ticket_helpdesk_backend.dto.AbsenceTypeResponse;
import com.example.ticket_helpdesk_backend.entity.AbsenceType;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AbsenceTypeRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AbsenceTypeService {

    private final AbsenceTypeRepository absenceTypeRepository;
    private final ModelMapper modelMapper;

    public List<AbsenceTypeResponse> getAll() {
        return absenceTypeRepository.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, AbsenceTypeResponse.class))
                .collect(Collectors.toList());
    }

    public AbsenceTypeResponse getById(UUID id) throws ResourceNotFoundException {
        AbsenceType absenceType = absenceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence type not found with id: " + id));
        return modelMapper.map(absenceType, AbsenceTypeResponse.class);
    }

    @Transactional
    public AbsenceTypeResponse create(AbsenceTypeRequest request) {
        AbsenceType absenceType = modelMapper.map(request, AbsenceType.class);
        absenceType.setCreatedAt(LocalDateTime.now());
        absenceType.setUpdatedAt(LocalDateTime.now());

        AbsenceType saved = absenceTypeRepository.save(absenceType);
        return modelMapper.map(saved, AbsenceTypeResponse.class);
    }

    @Transactional
    public AbsenceTypeResponse update(UUID id, AbsenceTypeRequest request) throws ResourceNotFoundException {
        AbsenceType existing = absenceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence type not found with id: " + id));

        modelMapper.map(request, existing);

        existing.setUpdatedAt(LocalDateTime.now());
        AbsenceType updated = absenceTypeRepository.save(existing);

        return modelMapper.map(updated, AbsenceTypeResponse.class);
    }

    @Transactional
    public void delete(UUID id) throws ResourceNotFoundException {
        if (!absenceTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Absence type not found with id: " + id);
        }
        absenceTypeRepository.deleteById(id);
    }
}
