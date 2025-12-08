package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionChangeRequestRepository extends JpaRepository<PositionChangeRequest, UUID> {
    List<PositionChangeRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    // Cho HR/Admin filter theo status (có thể mở rộng sau)
    List<PositionChangeRequest> findByStatusOrderByCreatedAtDesc(String status);
    // Lấy theo trạng thái
    List<PositionChangeRequest> findByStatusOrderByUpdatedAtDesc(String status);

}
