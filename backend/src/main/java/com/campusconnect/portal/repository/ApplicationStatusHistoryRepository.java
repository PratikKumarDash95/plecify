package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {
    List<ApplicationStatusHistory> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);
}
