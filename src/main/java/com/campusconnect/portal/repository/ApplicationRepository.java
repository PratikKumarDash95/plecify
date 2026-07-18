package com.campusconnect.portal.repository;

import com.campusconnect.portal.common.enums.ApplicationStatus;
import com.campusconnect.portal.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID>,
        JpaSpecificationExecutor<Application> {

    boolean existsByStudentIdAndJobId(UUID studentId, UUID jobId);

    @Query("""
            select a from Application a
            join fetch a.job j
            join fetch j.company c
            where a.student.id = :studentId
            """)
    Page<Application> findByStudentIdWithJob(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("""
            select a from Application a
            join fetch a.student s
            join fetch s.user u
            where a.job.id = :jobId
            """)
    Page<Application> findByJobIdWithStudent(@Param("jobId") UUID jobId, Pageable pageable);

    @Query("""
            select a from Application a
            join fetch a.job j
            join fetch j.company c
            join fetch a.student s
            join fetch s.user u
            where a.id = :id
            """)
    Optional<Application> findDetailById(@Param("id") UUID id);

    long countByJobId(UUID jobId);

    long countByJobIdAndStatus(UUID jobId, ApplicationStatus status);

    long countByStudentId(UUID studentId);

    long countByStudentIdAndStatus(UUID studentId, ApplicationStatus status);

    /** Aggregated status counts for a company's hiring-progress dashboard. */
    @Query("""
            select a.status, count(a) from Application a
            where a.job.company.id = :companyId
            group by a.status
            """)
    List<Object[]> countByStatusForCompany(@Param("companyId") UUID companyId);

    /** Total applications to jobs reviewed by a given placement cell — placement dashboard. */
    @Query("select count(a) from Application a where a.job.placementCell.id = :placementCellId")
    long countByPlacementCellId(@Param("placementCellId") UUID placementCellId);
}
