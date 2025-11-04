package com.example.ticket_helpdesk_backend.service.helper;

import com.example.ticket_helpdesk_backend.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomMatchScore {
    private Room room;
    private double score;
}
