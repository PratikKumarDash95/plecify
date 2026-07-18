package com.campusconnect.portal.service.eligibility;

import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.entity.EligibleJob;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.Student;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.JobRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.service.email.EmailService;
import com.campusconnect.portal.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Materialises the deterministic eligibility rules into the {@code eligible_jobs} table so the
 * student dashboard never recomputes eligibility at read time (the mandatory pre-computation
 * requirement).
 *
 * <p>Reconciliation semantics make runs idempotent:
 * <ul>
 *   <li>A matching student with no row (or a previously {@code REVOKED} row) gets an
 *       {@code ELIGIBLE} row and is notified.</li>
 *   <li>A matching student who already has an {@code ELIGIBLE}/{@code APPLIED} row is left
 *       untouched (no duplicate, no re-notification).</li>
 *   <li>A non-matching student holding an {@code ELIGIBLE} row has it {@code REVOKED};
 *       {@code APPLIED} rows are preserved to keep the application trail intact.</li>
 * </ul>
 *
 * The frontend link paths are relative so they work behind any host.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityEngineServiceImpl implements EligibilityEngineService {

    private final JobRepository jobRepository;
    private final StudentRepository studentRepository;
    private final EligibleJobRepository eligibleJobRepository;
    private final EligibilityRuleEvaluator evaluator;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final Clock clock = Clock.systemUTC();

    @Override
    @Transactional
    public EligibilityRunResult computeForJob(UUID jobId) {
        Job job = jobRepository.findDetailById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (job.getStatus() != JobStatus.APPROVED) {
            // Defensive: the engine only ever materialises visible (approved) jobs.
            log.warn("Eligibility engine invoked for job {} in status {} — skipping", jobId, job.getStatus());
            return new EligibilityRunResult(jobId, 0, 0, 0, 0, 0);
        }

        UUID universityId = job.getPlacementCell().getUniversity().getId();
        List<Student> candidates = studentRepository.findEligibilityCandidates(universityId);
        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);

        int matched = 0;
        int newlyMatched = 0;
        int revoked = 0;
        int notified = 0;

        for (Student student : candidates) {
            boolean eligible = evaluator.isEligible(job, student, today);
            EligibleJob existing = eligibleJobRepository
                    .findByStudentIdAndJobId(student.getId(), job.getId())
                    .orElse(null);

            if (eligible) {
                matched++;
                if (existing == null) {
                    eligibleJobRepository.save(EligibleJob.builder()
                            .student(student)
                            .job(job)
                            .status(EligibleJobStatus.ELIGIBLE)
                            .build());
                    newlyMatched++;
                    notified += notifyStudent(student, job);
                } else if (existing.getStatus() == EligibleJobStatus.REVOKED) {
                    // Re-qualified after a prior revoke — reactivate and re-notify.
                    existing.setStatus(EligibleJobStatus.ELIGIBLE);
                    eligibleJobRepository.save(existing);
                    newlyMatched++;
                    notified += notifyStudent(student, job);
                }
                // else already ELIGIBLE/APPLIED → leave untouched (idempotent).
            } else if (existing != null && existing.getStatus() == EligibleJobStatus.ELIGIBLE) {
                // No longer qualifies; revoke the open row. APPLIED rows are preserved.
                existing.setStatus(EligibleJobStatus.REVOKED);
                eligibleJobRepository.save(existing);
                revoked++;
            }
        }

        job.setEligibilityComputedAt(now);
        jobRepository.save(job);

        log.info("Eligibility for job {}: evaluated {}, matched {}, new {}, revoked {}",
                jobId, candidates.size(), matched, newlyMatched, revoked);
        return new EligibilityRunResult(jobId, candidates.size(), matched, newlyMatched, revoked, notified);
    }

    /**
     * Fires the in-app notification (on this transaction) and the async email side-channel.
     * Returns 1 so the caller can count dispatched notifications. Email failures never
     * propagate — {@link EmailService} swallows them.
     */
    private int notifyStudent(Student student, Job job) {
        String companyName = job.getCompany().getName();
        String link = "/jobs/" + job.getId();
        notificationService.notify(
                student.getUser().getId(),
                NotificationType.NEW_ELIGIBLE_JOB,
                "New opportunity: " + job.getTitle(),
                "A new job at " + companyName + " matches your profile.",
                link);
        emailService.sendNewEligibleJob(
                student.getUser().getEmail(),
                student.getUser().getFullName(),
                job.getTitle(),
                companyName,
                link);
        return 1;
    }
}
