package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InternalWorkHistoryRepository extends JpaRepository<InternalWorkHistory, UUID> {

    @EntityGraph(attributePaths = {"department", "position", "employeeProfile"})
    List<InternalWorkHistory> findByEmployeeProfileIdOrderByEffectiveDateDesc(UUID employeeId);
}
