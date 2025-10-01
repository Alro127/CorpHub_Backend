package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Ticket}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse implements Serializable {
    UUID id;
    TicketCategoryDto category;
    NameInfoDto requester;
    NameInfoDto assignee;
    DepartmentDto department;
    @Size(max = 255)
    String title;
    String description;
    String priority;
    String status;
    LocalDateTime assignedAt;
    LocalDateTime resolvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    static public TicketResponse toResponse(Ticket ticket) {
        TicketResponse dto = new TicketResponse();
        dto.setId(ticket.getId());
        dto.setCategory(new TicketCategoryDto(ticket.getCategory().getId(), ticket.getCategory().getName()));
        dto.setDepartment(new DepartmentDto(ticket.getDepartment().getId(), ticket.getDepartment().getName(), ticket.getDepartment().getDescription()));
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority().name());
        dto.setStatus(ticket.getStatus().name());
        dto.setAssignedAt(ticket.getAssignedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        // requester (luôn có)
        User requester = ticket.getRequester();
        dto.setRequester(new NameInfoDto(
                requester.getId(),
                requester.getEmployeeProfile().getFullName(),
                requester.getEmployeeProfile().getAvatar()
        ));

        // assignee (có thể null)
        User assignee = ticket.getAssignee();
        if (assignee != null) {
            dto.setAssignee(new NameInfoDto(
                    assignee.getId(),
                    assignee.getEmployeeProfile().getFullName(),
                    assignee.getEmployeeProfile().getAvatar()
            ));
        } else {
            dto.setAssignee(null);
        }

        return dto;
    }
}