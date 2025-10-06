package com.example.ticket_helpdesk_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MeetingRequest {
    private UUID id;
    private List<String> to;           // danh sách email người nhận
    private String subject;        // tiêu đề email
    private String title;          // tiêu đề sự kiện trong calendar
    private String description;    // mô tả sự kiện
    private boolean meetingRoom;   // có đặt phòng họp hay không
    private RoomRequirementDto roomRequirement; // yêu cầu đối với phòng họp
    private String location;       // địa điểm
    private String onlineLink;     // link họp online
    private LocalDateTime start;   // thời gian bắt đầu
    private LocalDateTime end;     // thời gian kết thúc
}
