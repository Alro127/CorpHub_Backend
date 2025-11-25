package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "external_work_history")
@Getter
@Setter
public class ExternalWorkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @Nationalized
    @Column(nullable = false, length = 255)
    private String companyName;

    @Nationalized
    @Column(nullable = false, length = 255)
    private String position; // chức danh tại công ty cũ

    private LocalDate startDate;
    private LocalDate endDate;

    @Nationalized
    @Column(columnDefinition = "nvarchar(max)")
    private String description; // mô tả công việc, dự án đã tham gia
}
