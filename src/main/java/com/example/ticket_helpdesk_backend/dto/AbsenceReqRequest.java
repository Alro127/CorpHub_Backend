package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link AbsenceRequest}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AbsenceReqRequest implements Serializable {
    UUID id;
    UUID absenceTypeId;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    String reason;
    @Size(max = 255)
    String attachmentUrl;
}