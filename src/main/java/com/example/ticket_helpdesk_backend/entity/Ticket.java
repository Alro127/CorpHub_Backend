package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.TicketStatus;
import com.example.ticket_helpdesk_backend.entity.Department;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private com.example.ticket_helpdesk_backend.entity.User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private com.example.ticket_helpdesk_backend.entity.User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Size(max = 255)
    @Nationalized
    @Column(name = "title")
    private String title;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Nationalized
    @Column(name = "priority", length = 50)
    private String priority;

    @Nationalized
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private TicketStatus status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}