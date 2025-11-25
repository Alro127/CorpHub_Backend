package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "competency_level")
@Getter
@Setter
public class CompetencyLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_type_id")
    private CompetencyType type;

    @Column(nullable = false)
    private String name; // VD: "Beginner", "Advanced", "C1"...

    @Column(name = "value_scale")
    private Integer valueScale; // để map về thang 5 khi vẽ biểu đồ rada
}

