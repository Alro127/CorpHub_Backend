package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.ITNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ITNotificationRepository extends JpaRepository<ITNotification, UUID> {
}
