package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>,
        JpaSpecificationExecutor<Company> {

    @EntityGraph(attributePaths = "user")
    Optional<Company> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<Company> findWithUserById(UUID id);

    @EntityGraph(attributePaths = "user")
    Page<Company> findByStatus(ApprovalStatus status, Pageable pageable);

    long countByStatus(ApprovalStatus status);
}
