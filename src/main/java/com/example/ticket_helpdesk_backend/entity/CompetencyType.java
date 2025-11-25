package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "competency_type")
@Getter
@Setter
public class CompetencyType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // VD: "CERTIFICATE", "SKILL"

    @Column(nullable = false)
    private String name; // VD: "Chứng chỉ", "Kỹ năng"

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetencyLevel> levels = new ArrayList<>();
}


