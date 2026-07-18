package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID>,
        JpaSpecificationExecutor<University> {

    Optional<University> findByCodeIgnoreCase(String code);
    Optional<University> findByEmailDomainIgnoreCase(String emailDomain);
    boolean existsByCodeIgnoreCase(String code);
}
