package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    @Query("SELECT t FROM Ticket t WHERE t.requester.id = :userId OR t.assignedTo.id = :userId")
    public List<Ticket> findMyTicketsByUserId(@Param("userId") Integer userId);

    @Query("SELECT t FROM Ticket t " +
            "WHERE (:title IS NULL OR t.title = :title)" +
            "AND (:category IS NULL OR t.category.id = :category) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:requesterId IS NULL OR t.requester.id = :requesterId) " +
            "AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)")
    List<Ticket> searchTickets( @Param("title") String title,
                                @Param("category") Integer category,
                                @Param("status") String status,
                                @Param("priority") String priority,
                                @Param("requesterId") Integer requesterId,
                                @Param("assignedToId") Integer assignedToId);
}