package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.ExportFormat;
import com.example.ticket_helpdesk_backend.consts.ExportLayout;
import com.example.ticket_helpdesk_backend.dto.ExportFileResult;
import com.example.ticket_helpdesk_backend.dto.WorkScheduleExportRequest;
import com.example.ticket_helpdesk_backend.entity.Shift;
import com.example.ticket_helpdesk_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class  WorkScheduleExportService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final ShiftRepository shiftRepository;

    private static final int MAX_CALENDAR_DAYS = 90; // guardrail 1-day delivery
    private static final DateTimeFormatter DATE_HEADER_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATE_RAW_FMT = DateTimeFormatter.ISO_DATE;

    @Transactional(readOnly = true)
    public ExportFileResult export(WorkScheduleExportRequest req) {

        normalize(req);
        validate(req);

        if (req.getFormat() == ExportFormat.CSV) {
            return exportCsv(req);
        }
        return exportExcel(req);
    }

    private void normalize(WorkScheduleExportRequest req) {
        if (req.getFormat() == null) req.setFormat(ExportFormat.EXCEL);
        if (req.getLayout() == null) req.setLayout(ExportLayout.CALENDAR);
        if (req.getIncludeShiftSheet() == null) req.setIncludeShiftSheet(true);
        if (req.getIncludeRawDataSheet() == null) req.setIncludeRawDataSheet(false);

        if (req.getFileName() == null || req.getFileName().isBlank()) {
            req.setFileName("work-schedule-" + req.getFromDate() + "-to-" + req.getToDate());
        }
    }

    private void validate(WorkScheduleExportRequest req) {
        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new IllegalArgumentException("fromDate must be <= toDate");
        }

        long days = req.getFromDate().until(req.getToDate()).getDays() + 1;
        boolean calendarNeeded = (req.getLayout() == ExportLayout.CALENDAR || req.getLayout() == ExportLayout.BOTH);

        if (calendarNeeded && days > MAX_CALENDAR_DAYS) {
            throw new IllegalArgumentException(
                    "Calendar layout supports up to " + MAX_CALENDAR_DAYS + " days for now. Please use layout=ROW or format=CSV."
            );
        }
    }

    private ExportFileResult exportExcel(WorkScheduleExportRequest req) {

        List<UUID> employeeIds = req.getEmployeeIds();
        if (employeeIds == null || employeeIds.isEmpty()) {
            employeeIds = null;
        }

        List<EmployeeScopeView> employees =
                employeeProfileRepository.findEmployeesForExport(req.getDepartmentId(), employeeIds);

        List<WorkScheduleExportRowView> rows =
                workScheduleRepository.findRowsForExport(req.getFromDate(), req.getToDate(), req.getDepartmentId(), employeeIds);

        // SXSSF streaming workbook; keep enough rows in memory to allow random access for 1000 employees
        try (SXSSFWorkbook wb = new SXSSFWorkbook(2000);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            wb.setCompressTempFiles(true);

            Map<UUID, Integer> empRowIndex = new HashMap<>(); // employeeId -> rowIndex (sheet row number)

            // Styles
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle normalStyle = createNormalStyle(wb);

            // 1) Calendar sheet (optional)
            if (req.getLayout() == ExportLayout.CALENDAR || req.getLayout() == ExportLayout.BOTH) {
                Sheet sh = wb.createSheet("Work Schedule");

                List<LocalDate> dates = buildDates(req.getFromDate(), req.getToDate());
                Map<LocalDate, Integer> dateToCol = new HashMap<>();

                // header: Code, FullName, then dates
                Row header = sh.createRow(0);
                createCell(header, 0, "Employee Code", headerStyle);
                createCell(header, 1, "Full Name", headerStyle);

                int col = 2;
                for (LocalDate d : dates) {
                    dateToCol.put(d, col);
                    createCell(header, col, d.format(DATE_HEADER_FMT), headerStyle);
                    col++;
                }

                // employee rows upfront
                int r = 1;
                for (EmployeeScopeView e : employees) {
                    Row row = sh.createRow(r);
                    empRowIndex.put(e.getEmployeeId(), r);

                    createCell(row, 0, nvl(e.getCode()), normalStyle);
                    createCell(row, 1, nvl(e.getFullName()), normalStyle);

                    // no need to create empty cells for dates (poi will keep blank)
                    r++;
                }

                // fill from schedules
                for (WorkScheduleExportRowView it : rows) {
                    Integer rowIdx = empRowIndex.get(it.getEmployeeId());
                    if (rowIdx == null) continue;

                    Integer colIdx = dateToCol.get(it.getWorkDate());
                    if (colIdx == null) continue;

                    Row empRow = sh.getRow(rowIdx);
                    if (empRow == null) continue;

                    String cellValue = buildCalendarCellValue(it);
                    Cell c = empRow.getCell(colIdx);
                    if (c == null) c = empRow.createCell(colIdx);
                    c.setCellValue(cellValue);
                    c.setCellStyle(normalStyle);
                }

                // Freeze top row + 2 first columns
                sh.createFreezePane(2, 1);


                // Set size of columns
                sh.setColumnWidth(0, 20 * 256); // Employee Code
                sh.setColumnWidth(1, 30 * 256); // Full Name
            }

            // 2) Raw rows sheet (optional)
            if (Boolean.TRUE.equals(req.getIncludeRawDataSheet())
                    || req.getLayout() == ExportLayout.ROW
                    || req.getLayout() == ExportLayout.BOTH) {

                Sheet raw = wb.createSheet("Raw Data");
                Row h = raw.createRow(0);

                String[] headers = {"Work Date", "Employee Code", "Full Name", "Shift", "Start", "End", "Status"};
                for (int i = 0; i < headers.length; i++) {
                    createCell(h, i, headers[i], headerStyle);
                }

                int r = 1;
                for (WorkScheduleExportRowView it : rows) {
                    Row row = raw.createRow(r++);

                    createCell(row, 0, it.getWorkDate() != null ? it.getWorkDate().format(DATE_RAW_FMT) : "", normalStyle);
                    createCell(row, 1, nvl(it.getEmployeeCode()), normalStyle);
                    createCell(row, 2, nvl(it.getEmployeeFullName()), normalStyle);
                    createCell(row, 3, nvl(it.getShiftName()), normalStyle);
                    createCell(row, 4, it.getShiftStartTime() != null ? it.getShiftStartTime().toString() : "", normalStyle);
                    createCell(row, 5, it.getShiftEndTime() != null ? it.getShiftEndTime().toString() : "", normalStyle);
                    createCell(row, 6, nvl(it.getStatus()), normalStyle);
                }

                raw.createFreezePane(0, 1);

                raw.setColumnWidth(0, 12 * 256); // Work Date
                raw.setColumnWidth(1, 18 * 256); // Employee Code
                raw.setColumnWidth(2, 28 * 256); // Full Name
                raw.setColumnWidth(3, 18 * 256); // Shift
                raw.setColumnWidth(4, 10 * 256); // Start
                raw.setColumnWidth(5, 10 * 256); // End
                raw.setColumnWidth(6, 14 * 256); // Status

            }

            // 3) Shift dictionary (optional)
            if (Boolean.TRUE.equals(req.getIncludeShiftSheet())) {
                Sheet shiftSheet = wb.createSheet("Shifts");
                Row h = shiftSheet.createRow(0);

                String[] headers = {"Shift Id", "Name", "Start", "End", "Night", "Flexible Minutes", "Working Hours"};
                for (int i = 0; i < headers.length; i++) {
                    createCell(h, i, headers[i], headerStyle);
                }

                List<Shift> shifts = shiftRepository.findAllByOrderByNameAsc();
                int r = 1;
                for (Shift s : shifts) {
                    Row row = shiftSheet.createRow(r++);
                    createCell(row, 0, s.getId() != null ? s.getId().toString() : "", normalStyle);
                    createCell(row, 1, nvl(s.getName()), normalStyle);
                    createCell(row, 2, s.getStartTime() != null ? s.getStartTime().toString() : "", normalStyle);
                    createCell(row, 3, s.getEndTime() != null ? s.getEndTime().toString() : "", normalStyle);
                    createCell(row, 4, s.getIsNightShift() != null ? String.valueOf(s.getIsNightShift()) : "false", normalStyle);
                    createCell(row, 5, s.getFlexibleMinutes() != null ? String.valueOf(s.getFlexibleMinutes()) : "", normalStyle);
                    createCell(row, 6, s.getWorkingHours() != null ? s.getWorkingHours().toPlainString() : "", normalStyle);
                }

                shiftSheet.createFreezePane(0, 1);
                shiftSheet.setColumnWidth(0, 36 * 256); // Shift Id
                shiftSheet.setColumnWidth(1, 20 * 256); // Name
                shiftSheet.setColumnWidth(2, 12 * 256); // Start
                shiftSheet.setColumnWidth(3, 12 * 256); // End
                shiftSheet.setColumnWidth(4, 10 * 256); // Night
                shiftSheet.setColumnWidth(5, 18 * 256); // Flexible Minutes
                shiftSheet.setColumnWidth(6, 16 * 256); // Working Hours

            }

            wb.write(out);

            String filename = ensureXlsx(req.getFileName());
            return new ExportFileResult(
                    filename,
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    new ByteArrayInputStream(out.toByteArray())
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel", e);
        }
    }

    private ExportFileResult exportCsv(WorkScheduleExportRequest req) {

        List<UUID> employeeIds = req.getEmployeeIds();
        if (employeeIds == null || employeeIds.isEmpty()) {
            employeeIds = null;
        }

        // CSV: làm nhanh, nhẹ, phù hợp range lớn
        List<WorkScheduleExportRowView> rows =
                workScheduleRepository.findRowsForExport(req.getFromDate(), req.getToDate(), req.getDepartmentId(),employeeIds);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            // UTF-8 BOM để Excel Windows nhận đúng encoding
            bw.write("\uFEFF");
            bw.write("work_date,employee_code,full_name,shift,start,end,status");

            bw.newLine();

            for (WorkScheduleExportRowView it : rows) {
                bw.write(csv(it.getWorkDate() != null ? it.getWorkDate().format(CSV_DATE_FMT) : ""));
                bw.write(",");
                bw.write(csv(nvl(it.getEmployeeCode())));
                bw.write(",");
                bw.write(csv(nvl(it.getEmployeeFullName())));
                bw.write(",");
                bw.write(csv(nvl(it.getShiftName())));
                bw.write(",");
                bw.write(csv(it.getShiftStartTime() != null ? it.getShiftStartTime().toString() : ""));
                bw.write(",");
                bw.write(csv(it.getShiftEndTime() != null ? it.getShiftEndTime().toString() : ""));
                bw.write(",");
                bw.write(csv(nvl(it.getStatus())));
                bw.newLine();
            }

            bw.flush();

            String filename = ensureCsv(req.getFileName());
            return new ExportFileResult(
                    filename,
                    MediaType.parseMediaType("text/csv"),
                    new ByteArrayInputStream(out.toByteArray())
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV", e);
        }
    }

    // ===== helpers =====

    private static final DateTimeFormatter CSV_DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private List<LocalDate> buildDates(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate d = from;
        while (!d.isAfter(to)) {
            dates.add(d);
            d = d.plusDays(1);
        }
        return dates;
    }

    private String buildCalendarCellValue(WorkScheduleExportRowView it) {
        // format gọn: "ShiftName (08:00-17:00)"
        String shift = nvl(it.getShiftName());
        String start = it.getShiftStartTime() != null ? it.getShiftStartTime().toString() : "";
        String end = it.getShiftEndTime() != null ? it.getShiftEndTime().toString() : "";
        String status = nvl(it.getStatus());

        // Bạn có thể đổi logic: ưu tiên status OFF/LEAVE thì hiển thị OFF
        if ("OFF".equalsIgnoreCase(status) || "LEAVE".equalsIgnoreCase(status)) {
            return status;
        }

        if (!start.isBlank() && !end.isBlank()) {
            return shift + " (" + start + "-" + end + ")";
        }
        return shift.isBlank() ? status : shift;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        if (style != null) c.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);

        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setWrapText(true);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle createNormalStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(false);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private String ensureXlsx(String name) {
        String base = name.trim();
        return base.toLowerCase().endsWith(".xlsx") ? base : (base + ".xlsx");
    }

    private String ensureCsv(String name) {
        String base = name.trim();
        return base.toLowerCase().endsWith(".csv") ? base : (base + ".csv");
    }

    private String csv(String s) {
        // escape basic CSV
        if (s == null) return "";
        boolean needQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needQuote ? ("\"" + escaped + "\"") : escaped;
    }
}
