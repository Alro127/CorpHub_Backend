package com.example.ticket_helpdesk_backend.consts;

public enum TicketStatus {
    OPEN, // Ticket vừa tạo, chờ trưởng phòng của requester xác nhận
    WAITING, // Ticket được chuyển sang phòng ban yêu cầu
    ASSIGNING, // Ticket được trưởng phòng ban yêu cầu phân cho nhân viên
    REJECTED, // Ticket bị từ chối bởi các trường phòng ban
    IN_PROGRESS, // Ticket được assignee chấp nhận
    DONE // Ticket hoàn thành
}
