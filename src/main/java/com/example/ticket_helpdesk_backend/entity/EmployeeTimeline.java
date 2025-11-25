package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_timeline")
@Getter
@Setter
public class EmployeeTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile;

    // 🔹 Liên kết đến loại sự kiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_type_id", nullable = false)
    private TimelineType timelineType;

    @Nationalized
    @Column(nullable = false, length = 255)
    private String title;

    @Nationalized
    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    private LocalDate date;

    @Nationalized
    @Column(length = 255)
    private String confirmedBy;

    @Nationalized
    @Column(length = 255)
    private String related;

    private String fileUrl;
}
