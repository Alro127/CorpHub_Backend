package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.RolePermission;
import com.example.ticket_helpdesk_backend.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}