package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID>,
        JpaSpecificationExecutor<Job> {

    /** Full aggregate load (company, cell, eligibility + its collections) for detail views and the engine. */
    @Query("""
            select j from Job j
            join fetch j.company c
            join fetch j.placementCell pc
            join fetch j.eligibility e
            where j.id = :id
            """)
    Optional<Job> findDetailById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"company", "placementCell"})
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "placementCell"})
    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "placementCell"})
    Page<Job> findByPlacementCellIdAndStatus(UUID placementCellId, JobStatus status, Pageable pageable);

    long countByStatus(JobStatus status);

    long countByCompanyId(UUID companyId);

    long countByCompanyIdAndStatus(UUID companyId, JobStatus status);

    long countByPlacementCellIdAndStatus(UUID placementCellId, JobStatus status);

    long countByPlacementCellId(UUID placementCellId);

    /**
     * Count of jobs for a cell in a given status whose review timestamp is at/after the cutoff.
     * Powers the placement dashboard's "approved/rejected today" widgets.
     */
    @Query("""
            select count(j) from Job j
            where j.placementCell.id = :placementCellId
              and j.status = :status
              and j.reviewedAt >= :since
            """)
    long countReviewedSince(@Param("placementCellId") UUID placementCellId,
                            @Param("status") JobStatus status,
                            @Param("since") Instant since);

    /** Approved jobs whose deadline has passed — used by the housekeeping scheduler. */
    @Query("select j from Job j where j.status = :status and j.applicationDeadline < :now")
    List<Job> findByStatusAndDeadlineBefore(@Param("status") JobStatus status, @Param("now") Instant now);
}
