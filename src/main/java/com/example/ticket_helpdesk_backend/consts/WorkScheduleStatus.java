package com.example.ticket_helpdesk_backend.consts;

public enum WorkScheduleStatus {
    SCHEDULED,    // Đã xếp lịch - chưa bắt đầu
    IN_PROGRESS,  // Đang làm - đã check-in
    COMPLETED,    // Đã hoàn thành ca - check-out
    MISSED,       // Không đi làm / không check-in
    CANCELLED,    // Admin hủy ca
    LEAVE         // Nghỉ phép (đã được duyệt)
}
