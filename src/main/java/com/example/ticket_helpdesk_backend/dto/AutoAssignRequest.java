package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.validation.ValidTimeRange;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidTimeRange
public class AutoAssignRequest {

    /** ID của ca làm việc cần phân */
    @NotNull(message = "Shift ID cannot be null")
    private UUID shiftId;

    /** Ngày bắt đầu phân ca */
    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    /** Ngày kết thúc phân ca */
    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    private LocalDate endDate;

    /** Danh sách nhân viên cụ thể (nếu chỉ định) */
    private List<UUID> userIds;

    /** Danh sách phòng ban để tự động lấy nhân viên */
    private List<UUID> departmentIds;

    /** true = ghi đè các ca đã có, false = bỏ qua các ca đã có */
    private Boolean replaceExisting = false;

    /** true = bỏ qua nhân viên đang nghỉ (absence, v.v.) */
    private Boolean respectAbsenceRequests = true;

    /** false = chỉ phân từ Thứ 2–Thứ 6 */
    private Boolean includeWeekend = false;
}
