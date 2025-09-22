package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SortDTO {
    private String field;  // Tên cột muốn sort, ví dụ "createDate", "priority"
    private String order;  // "asc" hoặc "desc"
}
