package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "internal_work_history")
@Getter
@Setter
public class InternalWorkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // phòng ban nhân viên được chuyển đến

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position; // chức danh được bổ nhiệm / thăng chức

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 100)
    private String changeType;
    // EX: TRANSFER / PROMOTION / DEMOTION / ASSIGNMENT

    @Column(length = 255)
    private String reason; // lý do: cơ cấu, thăng chức, điều chuyển...
}
