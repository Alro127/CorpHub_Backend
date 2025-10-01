package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ticket_comment")
public class TicketComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TicketComment parent;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "comment_text", nullable = false)
    private String commentText;

    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}