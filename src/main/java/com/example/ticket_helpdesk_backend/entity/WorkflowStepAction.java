package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.WorkflowActionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workflow_step_action")
public class WorkflowStepAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id", nullable = false)
    private WorkflowStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actor_id",
            nullable = true
    )
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"action\"", nullable = false, length = 30)
    private WorkflowActionType action;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "comment", length = 1000)
    private String comment;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}