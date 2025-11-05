package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentManagementDto {
    private UUID id;
    private String name;
    private String description;
    private UserBasicDto manager;

    // Convert DTO -> Entity (khi create hoặc update)
    public Department toEntity(EmployeeProfile managerEntity) {
        Department department = new Department();
        department.setId(this.id);
        department.setName(this.name);
        department.setDescription(this.description);
        department.setManager(managerEntity);
        return department;
    }

    // Convert Entity -> DTO (khi trả về response)
    public static DepartmentManagementDto fromEntity(Department department) {
        UserBasicDto managerDto = null;
        if (department.getManager() != null) {
            EmployeeProfile manager = department.getManager();
            managerDto = new UserBasicDto(
                    manager.getId(),
                    manager.getFullName(),
                    manager.getUser() != null ? manager.getUser().getUsername() : null,
                    manager.getAvatar()
            );
        }

        return new DepartmentManagementDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                managerDto
        );
    }
}
