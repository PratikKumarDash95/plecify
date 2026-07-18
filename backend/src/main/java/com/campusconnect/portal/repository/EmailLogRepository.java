package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
