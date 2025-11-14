package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {

    List<Position> findByDepartmentIdOrderByLevelOrderAsc(UUID departmentId);
}
