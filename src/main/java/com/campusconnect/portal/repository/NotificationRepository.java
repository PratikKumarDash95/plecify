package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(UUID recipientId, boolean read, Pageable pageable);

    long countByRecipientIdAndRead(UUID recipientId, boolean read);

    @Modifying
    @Query("""
            update Notification n set n.read = true, n.readAt = :now
            where n.recipient.id = :recipientId and n.read = false
            """)
    int markAllReadForRecipient(@Param("recipientId") UUID recipientId, @Param("now") Instant now);
}
