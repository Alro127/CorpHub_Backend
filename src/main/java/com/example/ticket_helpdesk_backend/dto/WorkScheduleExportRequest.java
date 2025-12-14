package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.ExportFormat;
import com.example.ticket_helpdesk_backend.consts.ExportLayout;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class WorkScheduleExportRequest {
    private String fileName;

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;

    private UUID departmentId;
    private List<UUID> employeeIds;

    private ExportFormat format; // EXCEL, CSV
    private ExportLayout layout; // CALENDAR, ROW, BOTH

    private Boolean includeShiftSheet = true;
    private Boolean includeRawDataSheet = false;
}



