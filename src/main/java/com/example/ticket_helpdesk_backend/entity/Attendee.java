package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.AttendeeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendees")
@Getter
@Setter
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attendee_id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @NotNull
    @Size(max = 255)
    @Nationalized
    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AttendeeStatus status = AttendeeStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
