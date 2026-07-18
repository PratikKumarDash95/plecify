package com.campusconnect.portal.service.placement;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.placement.ApproveJobResponse;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.PlacementCell;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.mapper.JobMapper;
import com.campusconnect.portal.repository.ApplicationRepository;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.JobRepository;
import com.campusconnect.portal.repository.PlacementCellRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.service.eligibility.EligibilityEngineService;
import com.campusconnect.portal.service.eligibility.EligibilityEngineService.EligibilityRunResult;
import com.campusconnect.portal.service.email.EmailService;
import com.campusconnect.portal.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Default {@link PlacementService}. Enforces that a cell only reviews jobs for its own
 * university, drives the PENDING→APPROVED/REJECTED transition, and — on approval — invokes
 * the deterministic eligibility engine within the same transaction so the job and its
 * pre-computed matches become visible atomically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementServiceImpl implements PlacementService {

    private final JobRepository jobRepository;
    private final PlacementCellRepository placementCellRepository;
    private final StudentRepository studentRepository;
    private final EligibleJobRepository eligibleJobRepository;
    private final ApplicationRepository applicationRepository;
    private final EligibilityEngineService eligibilityEngineService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final JobMapper jobMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> listPendingJobs(UUID placementUserId, Pageable pageable) {
        PlacementCell cell = requireCell(placementUserId);
        return jobRepository.findByPlacementCellIdAndStatus(cell.getId(), JobStatus.PENDING, pageable)
                .map(jobMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJob(UUID placementUserId, UUID jobId) {
        PlacementCell cell = requireCell(placementUserId);
        Job job = requireCellJob(jobId, cell);
        return toResponse(job);
    }

    @Override
    @Transactional
    public ApproveJobResponse approveJob(UUID placementUserId, UUID jobId) {
        PlacementCell cell = requireCell(placementUserId);
        Job job = requireCellJob(jobId, cell);
        requirePending(job);

        Instant now = Instant.now();
        job.setStatus(JobStatus.APPROVED);
        job.setApprovedBy(placementUserId);
        job.setApprovedAt(now);
        job.setReviewedAt(now);
        job.setRejectionReason(null);
        jobRepository.save(job);

        // Materialise eligibility now so students see the job immediately (pre-computation).
        EligibilityRunResult result = eligibilityEngineService.computeForJob(job.getId());

        notifyCompanyApproved(job, result.matched());

        log.info("Placement user {} approved job {} ({} students matched)",
                placementUserId, jobId, result.matched());
        return new ApproveJobResponse(job.getId(), job.getStatus().name(), placementUserId,
                now, result.matched(), result.notificationsDispatched());
    }

    @Override
    @Transactional
    public JobResponse rejectJob(UUID placementUserId, UUID jobId, String reason) {
        PlacementCell cell = requireCell(placementUserId);
        Job job = requireCellJob(jobId, cell);
        requirePending(job);

        Instant now = Instant.now();
        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(reason);
        job.setReviewedAt(now);
        jobRepository.save(job);

        notifyCompanyRejected(job, reason);

        log.info("Placement user {} rejected job {}", placementUserId, jobId);
        return toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PlacementDashboardResponse getDashboard(UUID placementUserId) {
        PlacementCell cell = requireCell(placementUserId);
        UUID cellId = cell.getId();
        UUID universityId = cell.getUniversity().getId();
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();

        long pending = jobRepository.countByPlacementCellIdAndStatus(cellId, JobStatus.PENDING);
        long approved = jobRepository.countByPlacementCellIdAndStatus(cellId, JobStatus.APPROVED);
        long rejected = jobRepository.countByPlacementCellIdAndStatus(cellId, JobStatus.REJECTED);
        long approvedToday = jobRepository.countReviewedSince(cellId, JobStatus.APPROVED, startOfDay);
        long rejectedToday = jobRepository.countReviewedSince(cellId, JobStatus.REJECTED, startOfDay);
        long totalStudents = studentRepository.countByUniversityId(universityId);
        long eligibleStudents = studentRepository.countByUniversityIdAndPlacementEligible(universityId, true);
        long applications = applicationRepository.countByPlacementCellId(cellId);

        return new PlacementDashboardResponse(pending, approvedToday, rejectedToday, approved,
                rejected, totalStudents, eligibleStudents, applications);
    }

    // ---------------------------------------------------------------- internals

    private PlacementCell requireCell(UUID placementUserId) {
        return placementCellRepository.findByUserId(placementUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No placement cell is associated with the current account"));
    }

    private Job requireCellJob(UUID jobId, PlacementCell cell) {
        Job job = jobRepository.findDetailById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPlacementCell().getId().equals(cell.getId())) {
            throw new BusinessRuleException(ErrorCode.ACCESS_DENIED,
                    "This job is not assigned to your placement cell for review");
        }
        return job;
    }

    private void requirePending(Job job) {
        if (job.getStatus() != JobStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "Only pending jobs can be reviewed; this job is " + job.getStatus());
        }
    }

    private void notifyCompanyApproved(Job job, int eligibleCount) {
        Company company = job.getCompany();
        String link = "/company/jobs/" + job.getId();
        notificationService.notify(company.getUser().getId(), NotificationType.JOB_APPROVED,
                "Job approved: " + job.getTitle(),
                "Your posting is now live and matched " + eligibleCount + " eligible student(s).",
                link);
        emailService.sendJobApproved(company.getUser().getEmail(), company.getUser().getFullName(),
                job.getTitle(), eligibleCount, link);
    }

    private void notifyCompanyRejected(Job job, String reason) {
        Company company = job.getCompany();
        String link = "/company/jobs/" + job.getId();
        notificationService.notify(company.getUser().getId(), NotificationType.JOB_REJECTED,
                "Job not approved: " + job.getTitle(),
                "Reason: " + reason,
                link);
        emailService.sendJobRejected(company.getUser().getEmail(), company.getUser().getFullName(),
                job.getTitle(), reason, link);
    }

    private JobResponse toResponse(Job job) {
        long eligible = eligibleJobRepository.countByJobId(job.getId());
        long applications = applicationRepository.countByJobId(job.getId());
        return jobMapper.toResponse(job, eligible, applications);
    }
}
