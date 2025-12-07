package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.AssetStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "asset")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private com.example.ticket_helpdesk_backend.entity.AssetCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssetStatus status;

    @Column(name = "\"value\"", precision = 18, scale = 2)
    private BigDecimal value;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty")
    private LocalDate warranty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

}