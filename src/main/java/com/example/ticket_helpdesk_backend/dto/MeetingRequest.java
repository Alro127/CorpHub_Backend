package com.example.ticket_helpdesk_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeetingRequest {
    private List<String> to;           // danh sách email người nhận
    private String subject;        // tiêu đề email
    private String title;          // tiêu đề sự kiện trong calendar
    private String description;    // mô tả sự kiện
    private String location;       // địa điểm
    private String onlineLink;     // link họp online
    private LocalDateTime start;   // thời gian bắt đầu
    private LocalDateTime end;     // thời gian kết thúc
    private String organizer;      // email người tổ chức
}
