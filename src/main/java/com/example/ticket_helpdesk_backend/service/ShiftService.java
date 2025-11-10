package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.ShiftDto;
import com.example.ticket_helpdesk_backend.entity.Shift;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.ShiftRepository;
import com.example.ticket_helpdesk_backend.specification.ShiftSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper;

    public Page<ShiftDto> getAll(int page, int size, String keywords, Boolean isNightShift,
                                 LocalTime startFrom, LocalTime endTo) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());

        Specification<Shift> spec = Specification
                .where(ShiftSpecifications.hasKeyword(keywords))
                .and(ShiftSpecifications.isNightShift(isNightShift))
                .and(ShiftSpecifications.startAfter(startFrom))
                .and(ShiftSpecifications.endBefore(endTo));

        return shiftRepository.findAll(spec, pageable).map((element) -> modelMapper.map(element, ShiftDto.class));
    }

    public ShiftDto getById(UUID id) throws ResourceNotFoundException {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id " + id));
        return modelMapper.map(shift, ShiftDto.class);
    }

    public ShiftDto create(ShiftDto dto) {
        Shift entity = modelMapper.map(dto, Shift.class);
        Shift saved = shiftRepository.save(entity);
        return modelMapper.map(saved, ShiftDto.class);
    }

    public ShiftDto update(UUID id, ShiftDto dto) throws ResourceNotFoundException {
        Shift existing = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id " + id));

        modelMapper.map(dto, existing);
        Shift updated = shiftRepository.save(existing);
        return modelMapper.map(updated, ShiftDto.class);
    }

    public ShiftDto delete(UUID id) throws ResourceNotFoundException {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id " + id));
        shiftRepository.delete(shift);
        return modelMapper.map(shift, ShiftDto.class);
    }
}
