package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "position_change_attachment")
@Getter
@Setter
public class PositionChangeAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private PositionChangeRequest request;

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl; // link MinIO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private EmployeeProfile uploadedBy;

    private LocalDateTime uploadedAt;
}
