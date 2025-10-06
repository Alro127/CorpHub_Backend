package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class AssetRequest implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String name;
    @NotNull
    @Size(max = 50)
    String code;
    @Size(max = 50)
    String category;
    @NotNull
    @Size(max = 20)
    String status;
    BigDecimal value;
    LocalDate purchaseDate;
    LocalDate warranty;
    UUID roomId;
}