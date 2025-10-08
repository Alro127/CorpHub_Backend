package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  @Query(value = """
    SELECT t.*
    FROM ticket t
    LEFT JOIN ticket_rejection tr ON tr.ticket_id = t.id
    WHERE t.department_id = :departmentId
      AND (
        -- Lấy tất cả ticket không phải OPEN và không phải REJECTED
        (t.status NOT IN ('OPEN', 'REJECTED'))
        OR
        -- Hoặc là REJECTED nhưng người reject thuộc cùng phòng ban
        (t.status = 'REJECTED'
         AND tr.rejected_by IN (
            SELECT u.id
            FROM "user" u
            JOIN employee_profile e ON u.id = e.id
            WHERE e.department_id = :departmentId
         )
        )
      )
    ORDER BY t.created_at DESC
    """, nativeQuery = true)
  List<Ticket> findReceivedTicketByDepartmentId(@Param("departmentId") UUID departmentId);

  @Query(value = """
    SELECT t.* 
    FROM ticket t
    JOIN [user] u ON t.requester_id = u.id
    JOIN employee_profile e ON u.id = e.id
    WHERE e.department_id = :departmentId
    ORDER BY t.created_at DESC
    """, nativeQuery = true)
  List<Ticket> findSentTicketByDepartmentId(@Param("departmentId") UUID departmentId);

  @Query(value = """
    SELECT t.* 
    FROM ticket t
    WHERE t.requester_id = :userId OR t.assignee_id = :userId
    ORDER BY t.created_at DESC
    """, nativeQuery = true)
  List<Ticket> findMyTickets(@Param("userId") UUID id);
}