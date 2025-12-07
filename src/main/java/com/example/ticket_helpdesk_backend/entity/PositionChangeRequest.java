package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "position_change_request")
@Getter
@Setter
public class PositionChangeRequest {

    public static final String STATUS_DRAFT      = "DRAFT";
    public static final String STATUS_PENDING    = "PENDING";   // chờ bước đầu (manager)
    public static final String STATUS_IN_REVIEW  = "IN_REVIEW"; // đang phê duyệt (>= step 2)
    public static final String STATUS_REJECTED   = "REJECTED";
    public static final String STATUS_FINALIZED  = "FINALIZED"; // đã duyệt xong + có thể upload quyết định
    public static final String STATUS_DONE = "DONE"; // HR upload file công bố

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_position_id")
    private Position oldPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_position_id", nullable = false)
    private Position newPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_department_id")
    private Department oldDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_department_id", nullable = false)
    private Department newDepartment;

    @Column(length = 50, nullable = false)
    private String type; // promotion / transfer / rotation / demotion / assignment

    @Column(name = "effect_date", nullable = false)
    private LocalDate effectDate;

    @Column(length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private EmployeeProfile createdBy;

    @Column(length = 50, nullable = false)
    private String status;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PositionChangeAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<PositionChangeApproval> approvals = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper
    public boolean isFinalized() {
        return STATUS_FINALIZED.equals(this.status);
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equals(this.status);
    }
}
