package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.entity.EligibleJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EligibleJobRepository extends JpaRepository<EligibleJob, UUID> {

    /**
     * The student dashboard's primary read: eligible jobs for a student in a given status,
     * with job + company fetched to render cards without N+1.
     */
    @Query(value = """
            select ej from EligibleJob ej
            join fetch ej.job j
            join fetch j.company c
            where ej.student.id = :studentId and ej.status = :status
            """,
            countQuery = """
            select count(ej) from EligibleJob ej
            where ej.student.id = :studentId and ej.status = :status
            """)
    Page<EligibleJob> findDashboardJobs(@Param("studentId") UUID studentId,
                                        @Param("status") EligibleJobStatus status,
                                        Pageable pageable);

    Optional<EligibleJob> findByStudentIdAndJobId(UUID studentId, UUID jobId);

    boolean existsByStudentIdAndJobIdAndStatus(UUID studentId, UUID jobId, EligibleJobStatus status);

    long countByStudentIdAndStatus(UUID studentId, EligibleJobStatus status);

    long countByJobId(UUID jobId);

    /** Upcoming deadlines for the student dashboard widget. */
    @Query("""
            select ej from EligibleJob ej
            join fetch ej.job j
            join fetch j.company c
            where ej.student.id = :studentId
              and ej.status = com.campusconnect.portal.common.enums.EligibleJobStatus.ELIGIBLE
              and j.applicationDeadline > :now
            order by j.applicationDeadline asc
            """)
    java.util.List<EligibleJob> findUpcomingDeadlines(@Param("studentId") UUID studentId,
                                                      @Param("now") Instant now,
                                                      Pageable pageable);

    /** Idempotent bulk revoke when a job is closed/expired. */
    @Modifying
    @Query("""
            update EligibleJob ej
            set ej.status = com.campusconnect.portal.common.enums.EligibleJobStatus.REVOKED,
                ej.updatedAt = :now
            where ej.job.id = :jobId
              and ej.status = com.campusconnect.portal.common.enums.EligibleJobStatus.ELIGIBLE
            """)
    int revokeAllForJob(@Param("jobId") UUID jobId, @Param("now") Instant now);
}
