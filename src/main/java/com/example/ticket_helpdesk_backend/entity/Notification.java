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
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newsequentialid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    @Size(max = 500)
    @Nationalized
    @Column(name = "link", length = 500)
    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Size(max = 50)
    @Nationalized
    @Column(name = "type", length = 50)
    private String type;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Nationalized
    @Lob
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "is_read", insertable = false, updatable = false)
    private Integer isRead;

}