package com.campusconnect.portal.repository;

import com.campusconnect.portal.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>,
        JpaSpecificationExecutor<Student> {

    @EntityGraph(attributePaths = {"user", "university"})
    Optional<Student> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "university", "skills"})
    Optional<Student> findWithSkillsById(UUID id);

    boolean existsByUniversityIdAndRollNumberIgnoreCase(UUID universityId, String rollNumber);

    /**
     * Candidate pool for the eligibility engine: only placement-eligible students of the
     * target university, fetched with skills to avoid N+1 during matching. Coarse SQL-level
     * filtering (university + master flag) is done here; the fine-grained deterministic rules
     * are applied in {@code EligibilityEngineService}.
     */
    @Query("""
            select distinct s from Student s
            left join fetch s.skills
            join fetch s.user u
            where s.university.id = :universityId
              and s.placementEligible = true
              and u.enabled = true
            """)
    List<Student> findEligibilityCandidates(@Param("universityId") UUID universityId);

    long countByUniversityId(UUID universityId);

    long countByUniversityIdAndPlacementEligible(UUID universityId, boolean placementEligible);
}
