package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.HolidayCalendarRequest;
import com.example.ticket_helpdesk_backend.dto.HolidayCalendarResponse;
import com.example.ticket_helpdesk_backend.entity.HolidayCalendar;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.HolidayCalendarRepository;
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
public class HolidayCalendarService {

    private final HolidayCalendarRepository holidayCalendarRepository;
    private final ModelMapper modelMapper;

    public List<HolidayCalendarResponse> getAll() {
        return holidayCalendarRepository.findAll().stream().map((element) -> modelMapper.map(element, HolidayCalendarResponse.class)).collect(Collectors.toList());
    }

    public HolidayCalendarResponse getById(UUID id) throws ResourceNotFoundException {
        HolidayCalendar holidayCalendar = holidayCalendarRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Holiday not found with id: " + id)
        );
        return modelMapper.map(holidayCalendar, HolidayCalendarResponse.class);
    }

    @Transactional
    public HolidayCalendarResponse create(HolidayCalendarRequest request) {
        HolidayCalendar holidayCalendar = modelMapper.map(request, HolidayCalendar.class);
        holidayCalendar.setCreatedAt(LocalDateTime.now());
        holidayCalendarRepository.save(holidayCalendar);

        return modelMapper.map(holidayCalendar, HolidayCalendarResponse.class);
    }

    @Transactional
    public HolidayCalendarResponse update(UUID id, HolidayCalendarRequest request) throws ResourceNotFoundException {
        HolidayCalendar existing = holidayCalendarRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Holiday not found with id: " + id)
        );

        modelMapper.map(request, existing);

        HolidayCalendar updated = holidayCalendarRepository.save(existing);
        return modelMapper.map(updated, HolidayCalendarResponse.class);
    }

    @Transactional
    public void delete(UUID id) throws ResourceNotFoundException {
        if (!holidayCalendarRepository.existsById(id)) {
            throw new ResourceNotFoundException("Holiday not found with id: " + id);
        }
        holidayCalendarRepository.deleteById(id);
    }
}
