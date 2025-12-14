package com.example.ticket_helpdesk_backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface WorkScheduleExportRowView {
    UUID getEmployeeId();
    String getEmployeeCode();
    String getEmployeeFullName();

    LocalDate getWorkDate();

    UUID getShiftId();
    String getShiftName();
    LocalTime getShiftStartTime();
    LocalTime getShiftEndTime();

    String getStatus(); // enum -> string
}
