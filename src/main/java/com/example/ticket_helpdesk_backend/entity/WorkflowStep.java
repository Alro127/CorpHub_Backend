package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.consts.WorkflowStepType;
import com.example.ticket_helpdesk_backend.model.ApproverDefinition;
import com.example.ticket_helpdesk_backend.service.helper.ApproverDefinitionConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workflow_step")
public class WorkflowStep {
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
    @JoinColumn(name = "template_id", nullable = false)
    private WorkflowTemplate template;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotNull
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private WorkflowStepType stepType;

    @Convert(converter = ApproverDefinitionConverter.class)
    @Column(name = "approver", columnDefinition = "NVARCHAR(MAX)")
    private ApproverDefinition approver;

    @Nationalized
    @Lob
    @Column(name = "condition_expr")
    private String conditionExpr;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}