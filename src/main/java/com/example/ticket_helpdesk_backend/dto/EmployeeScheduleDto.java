package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeScheduleDto {
    private UUID id;
    private String name;
    private String department;
    private List<EmployeeShiftDto> shifts;
}

