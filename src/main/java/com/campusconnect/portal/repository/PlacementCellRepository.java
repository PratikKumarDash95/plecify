package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.PlacementCell;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlacementCellRepository extends JpaRepository<PlacementCell, UUID> {

    @EntityGraph(attributePaths = {"user", "university"})
    Optional<PlacementCell> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "university"})
    Optional<PlacementCell> findByUniversityId(UUID universityId);
}
