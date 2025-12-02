package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "position_change_approval")
@Getter
@Setter
public class PositionChangeApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private PositionChangeRequest request;

    private Integer stepOrder; // 1,2,3,...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private EmployeeProfile approver;

    @Column(length = 50, nullable = false)
    private String role; // MANAGER / HR / ADMIN

    @Column(length = 50, nullable = false)
    private String decision; // pending / approved / rejected

    @Column(length = 255)
    private String comment;

    private LocalDateTime decidedAt;
}

