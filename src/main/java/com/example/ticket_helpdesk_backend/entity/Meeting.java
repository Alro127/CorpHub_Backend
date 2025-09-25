package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter
@Setter
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "meeting_id", nullable = false)
    private UUID id;

    @NotNull
    @Size(max = 255)
    @Nationalized
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Size(max = 255)
    @Nationalized
    @Column(name = "subject", nullable = false)
    private String subject;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Nationalized
    @Column(name = "location")
    private String location;

    @Size(max = 500)
    @Nationalized
    @Column(name = "online_link", length = 500)
    private String onlineLink;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Size(max = 255)
    @Nationalized
    @Column(name = "organizer_email", nullable = false)
    private String organizerEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendee> attendees = new ArrayList<>();
}
