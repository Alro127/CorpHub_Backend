package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "absence_type")
public class AbsenceType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @ColumnDefault("0")
    @Column(name = "require_proof")
    private Boolean requireProof;

    @ColumnDefault("1")
    @Column(name = "require_approval_lv")
    private Integer requireApprovalLv;

    @ColumnDefault("1")
    @Column(name = "affect_quota")
    private Boolean affectQuota;

    @ColumnDefault("NULL")
    @Column(name = "max_per_request", precision = 5, scale = 2)
    private BigDecimal maxPerRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_limit", length = 10)
    private Gender genderLimit;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_template_id")
    private WorkflowTemplate workflowTemplate;
}