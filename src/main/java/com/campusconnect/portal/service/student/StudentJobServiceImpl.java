package com.campusconnect.portal.service.student;

import com.campusconnect.portal.common.enums.ApplicationStatus;
import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.dashboard.StudentDashboardResponse;
import com.campusconnect.portal.dto.student.ApplicationResponse;
import com.campusconnect.portal.dto.student.ApplyJobRequest;
import com.campusconnect.portal.dto.student.EligibleJobResponse;
import com.campusconnect.portal.entity.Application;
import com.campusconnect.portal.entity.ApplicationStatusHistory;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.EligibleJob;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.Student;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.mapper.ApplicationMapper;
import com.campusconnect.portal.mapper.JobMapper;
import com.campusconnect.portal.repository.ApplicationRepository;
import com.campusconnect.portal.repository.ApplicationStatusHistoryRepository;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.service.email.EmailService;
import com.campusconnect.portal.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Default {@link StudentJobService}. Reads the student dashboard straight from the
 * pre-computed {@code eligible_jobs} table and gates applications on the presence of an
 * {@code ELIGIBLE} row — so eligibility policy lives entirely in the engine, never here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentJobServiceImpl implements StudentJobService {

    /** Number of upcoming-deadline items surfaced on the dashboard widget. */
    private static final int UPCOMING_DEADLINE_LIMIT = 5;

    private final StudentRepository studentRepository;
    private final EligibleJobRepository eligibleJobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final JobMapper jobMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EligibleJobResponse> listEligibleJobs(UUID studentUserId, Pageable pageable) {
        Student student = requireStudent(studentUserId);
        return eligibleJobRepository
                .findDashboardJobs(student.getId(), EligibleJobStatus.ELIGIBLE, pageable)
                .map(jobMapper::toEligibleJobResponse);
    }

    @Override
    @Transactional
    public ApplicationResponse applyToJob(UUID studentUserId, UUID jobId, ApplyJobRequest request) {
        Student student = requireStudent(studentUserId);

        EligibleJob eligibleJob = eligibleJobRepository
                .findByStudentIdAndJobId(student.getId(), jobId)
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.NOT_ELIGIBLE,
                        "You are not eligible to apply for this job"));

        switch (eligibleJob.getStatus()) {
            case APPLIED -> throw new BusinessRuleException(ErrorCode.DUPLICATE_APPLICATION);
            case REVOKED -> throw new BusinessRuleException(ErrorCode.NOT_ELIGIBLE,
                    "You are no longer eligible to apply for this job");
            case ELIGIBLE -> { /* proceed */ }
        }

        Job job = eligibleJob.getJob();
        if (!job.isOpenForApplications()) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "This job is not currently open for applications");
        }

        // Guard against a race with a duplicate row lacking the eligibility gate.
        if (applicationRepository.existsByStudentIdAndJobId(student.getId(), jobId)) {
            throw new BusinessRuleException(ErrorCode.DUPLICATE_APPLICATION);
        }

        Instant now = Instant.now();
        String resumeUrl = request.resumeUrl() != null ? request.resumeUrl() : student.getResumeUrl();
        Application application = Application.builder()
                .student(student)
                .job(job)
                .status(ApplicationStatus.APPLIED)
                .resumeUrl(resumeUrl)
                .coverLetter(request.coverLetter())
                .lastStatusChangeAt(now)
                .build();
        Application saved = applicationRepository.save(application);

        statusHistoryRepository.save(ApplicationStatusHistory.builder()
                .application(saved)
                .fromStatus(null)
                .toStatus(ApplicationStatus.APPLIED)
                .note("Application submitted")
                .changedBy(student.getUser().getEmail())
                .build());

        // Flip the eligibility row so the dashboard separates applied from open without a join.
        eligibleJob.setStatus(EligibleJobStatus.APPLIED);
        eligibleJobRepository.save(eligibleJob);

        notifyOnApply(student, job);

        log.info("Student {} applied to job {}", student.getId(), jobId);
        // Re-read with associations initialised for a clean response projection.
        return applicationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(UUID studentUserId, Pageable pageable) {
        Student student = requireStudent(studentUserId);
        return applicationRepository.findByStudentIdWithJob(student.getId(), pageable)
                .map(applicationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard(UUID studentUserId) {
        Student student = requireStudent(studentUserId);
        long eligible = eligibleJobRepository.countByStudentIdAndStatus(
                student.getId(), EligibleJobStatus.ELIGIBLE);
        long applied = applicationRepository.countByStudentId(student.getId());
        long interviews = applicationRepository.countByStudentIdAndStatus(
                student.getId(), ApplicationStatus.INTERVIEW_SCHEDULED);
        List<EligibleJobResponse> upcoming = eligibleJobRepository
                .findUpcomingDeadlines(student.getId(), Instant.now(),
                        PageRequest.of(0, UPCOMING_DEADLINE_LIMIT))
                .stream()
                .map(jobMapper::toEligibleJobResponse)
                .toList();
        return new StudentDashboardResponse(
                student.isPlacementEligible(), eligible, applied, interviews, upcoming);
    }

    // ---------------------------------------------------------------- internals

    private Student requireStudent(UUID studentUserId) {
        return studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student profile is associated with the current account"));
    }

    private void notifyOnApply(Student student, Job job) {
        Company company = job.getCompany();
        String studentName = student.getUser().getFullName();

        // Notify the student (confirmation).
        notificationService.notify(student.getUser().getId(), NotificationType.APPLICATION_SUBMITTED,
                "Application submitted: " + job.getTitle(),
                "Your application to " + company.getName() + " has been received.",
                "/applications");

        // Notify the company (new applicant).
        String companyLink = "/company/jobs/" + job.getId() + "/applications";
        notificationService.notify(company.getUser().getId(), NotificationType.APPLICATION_RECEIVED,
                "New application: " + job.getTitle(),
                studentName + " has applied to your posting.",
                companyLink);
        emailService.sendApplicationReceived(company.getUser().getEmail(),
                company.getUser().getFullName(), studentName, job.getTitle(), companyLink);
    }
}
