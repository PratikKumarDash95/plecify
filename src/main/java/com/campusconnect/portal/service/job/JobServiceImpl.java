package com.campusconnect.portal.service.job;

import com.campusconnect.portal.common.enums.ApplicationStatus;
import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.dto.dashboard.CompanyDashboardResponse;
import com.campusconnect.portal.dto.job.CreateJobRequest;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.job.UpdateJobRequest;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.PlacementCell;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.mapper.JobMapper;
import com.campusconnect.portal.repository.ApplicationRepository;
import com.campusconnect.portal.repository.CompanyRepository;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.JobRepository;
import com.campusconnect.portal.repository.PlacementCellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default {@link JobService}. Owns the company side of the job lifecycle: creation into
 * {@code PENDING}, edits while pending, and owner-scoped reads. Ownership is enforced by
 * matching the job's company against the authenticated company profile, so one company can
 * never read or mutate another's postings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final PlacementCellRepository placementCellRepository;
    private final EligibleJobRepository eligibleJobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(UUID companyUserId, CreateJobRequest request) {
        Company company = requireCompany(companyUserId);
        if (!company.isApproved()) {
            // A company can register and log in while PENDING, but cannot post jobs until an
            // admin approves it (see AdminService#approveCompany).
            throw new BusinessRuleException(ErrorCode.ACCOUNT_NOT_APPROVED,
                    "Your company is pending admin approval and cannot post jobs yet.");
        }
        PlacementCell cell = placementCellRepository.findByUniversityId(request.universityId())
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.BAD_REQUEST,
                        "The selected university does not have a placement cell to review this posting"));

        Job job = jobMapper.toEntity(request);
        job.setCompany(company);
        job.setPlacementCell(cell);
        job.setStatus(JobStatus.PENDING);

        Job saved = jobRepository.save(job);
        log.info("Company {} created job {} targeting university {} (PENDING)",
                company.getId(), saved.getId(), request.universityId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID companyUserId, UUID jobId, UpdateJobRequest request) {
        Company company = requireCompany(companyUserId);
        Job job = requireOwnedJob(jobId, company.getId());

        if (job.getStatus() != JobStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "Only pending jobs can be edited; this job is " + job.getStatus());
        }

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setJobType(request.jobType());
        job.setLocation(request.location());
        job.setRemoteAllowed(request.remoteAllowed());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setCurrency(request.currency() != null ? request.currency() : "USD");
        job.setOpenings(request.openings() != null ? request.openings() : 1);
        job.setApplicationDeadline(request.applicationDeadline());
        jobMapper.applyEligibility(job.getEligibility(), request.eligibility());

        Job saved = jobRepository.save(job);
        log.info("Company {} updated pending job {}", company.getId(), jobId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getCompanyJob(UUID companyUserId, UUID jobId) {
        Company company = requireCompany(companyUserId);
        Job job = requireOwnedJob(jobId, company.getId());
        return toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> listCompanyJobs(UUID companyUserId, Pageable pageable) {
        Company company = requireCompany(companyUserId);
        return jobRepository.findByCompanyId(company.getId(), pageable)
                .map(jobMapper::toSummary);
    }

    @Override
    @Transactional
    public void deleteJob(UUID companyUserId, UUID jobId) {
        Company company = requireCompany(companyUserId);
        Job job = requireOwnedJob(jobId, company.getId());
        if (job.getStatus() != JobStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "Only pending jobs can be deleted; this job is " + job.getStatus());
        }
        jobRepository.delete(job);
        log.info("Company {} deleted pending job {}", company.getId(), jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDashboardResponse getCompanyDashboard(UUID companyUserId) {
        Company company = requireCompany(companyUserId);
        UUID companyId = company.getId();

        long total = jobRepository.countByCompanyId(companyId);
        long pending = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.PENDING);
        long approved = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.APPROVED);
        long rejected = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.REJECTED);
        long closed = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.CLOSED);

        Map<String, Long> byStatus = new HashMap<>();
        long totalApplications = 0;
        for (Object[] row : applicationRepository.countByStatusForCompany(companyId)) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = (Long) row[1];
            byStatus.put(status.name(), count);
            totalApplications += count;
        }

        return new CompanyDashboardResponse(total, pending, approved, rejected, closed,
                totalApplications, byStatus);
    }

    // ---------------------------------------------------------------- internals

    private Company requireCompany(UUID companyUserId) {
        return companyRepository.findByUserId(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No company profile is associated with the current account"));
    }

    private Job requireOwnedJob(UUID jobId, UUID companyId) {
        Job job = jobRepository.findDetailById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getCompany().getId().equals(companyId)) {
            // Do not leak existence of other companies' jobs — surface as access denied.
            throw new BusinessRuleException(ErrorCode.ACCESS_DENIED,
                    "You do not have permission to access this job");
        }
        return job;
    }

    private JobResponse toResponse(Job job) {
        long eligible = eligibleJobRepository.countByJobId(job.getId());
        long applications = applicationRepository.countByJobId(job.getId());
        return jobMapper.toResponse(job, eligible, applications);
    }
}
