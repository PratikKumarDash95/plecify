package com.campusconnect.portal.service.eligibility;

import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.EligibleJob;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.PlacementCell;
import com.campusconnect.portal.entity.Student;
import com.campusconnect.portal.entity.University;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.JobRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.service.email.EmailService;
import com.campusconnect.portal.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Reconciliation-behaviour tests for the eligibility engine. The rule evaluation itself is
 * covered exhaustively in {@link EligibilityRuleEvaluatorTest}; here we use a real evaluator
 * and stub the repositories to assert the create/revoke/idempotency semantics and the
 * notification side-effects.
 */
@ExtendWith(MockitoExtension.class)
class EligibilityEngineServiceImplTest {

    @Mock private JobRepository jobRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EligibleJobRepository eligibleJobRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    // Real evaluator — deterministic, no need to mock.
    private final EligibilityRuleEvaluator evaluator = new EligibilityRuleEvaluator();

    private EligibilityEngineServiceImpl engine;

    private static final UUID JOB_ID = UUID.randomUUID();
    private static final UUID UNIVERSITY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        engine = new EligibilityEngineServiceImpl(jobRepository, studentRepository,
                eligibleJobRepository, evaluator, notificationService, emailService);
    }

    private Job approvedJob() {
        University uni = University.builder().id(UNIVERSITY_ID).name("MIT").build();
        PlacementCell cell = PlacementCell.builder().id(UUID.randomUUID()).university(uni).build();
        User companyUser = User.builder().id(UUID.randomUUID()).email("hr@acme.io").fullName("Acme HR").build();
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme").user(companyUser).build();
        Job job = Job.builder()
                .id(JOB_ID)
                .status(JobStatus.APPROVED)
                .title("SWE")
                .company(company)
                .placementCell(cell)
                .build();
        job.setEligibility(com.campusconnect.portal.entity.JobEligibility.builder().build()); // no constraints
        return job;
    }

    private Student student(String email) {
        User user = User.builder().id(UUID.randomUUID()).email(email).fullName("Stu").build();
        return Student.builder()
                .id(UUID.randomUUID())
                .user(user)
                .cgpa(new java.math.BigDecimal("9.0"))
                .placementEligible(true)
                .build();
    }

    @Test
    void computeForJob_throwsWhenJobMissing() {
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> engine.computeForJob(JOB_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void computeForJob_skipsWhenNotApproved() {
        Job job = approvedJob();
        job.setStatus(JobStatus.PENDING);
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        var result = engine.computeForJob(JOB_ID);

        assertThat(result.matched()).isZero();
        verifyNoInteractions(studentRepository, eligibleJobRepository, notificationService);
    }

    @Test
    void computeForJob_insertsRowAndNotifiesForNewMatch() {
        Job job = approvedJob();
        Student stu = student("a@mit.edu");
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(stu));
        when(eligibleJobRepository.findByStudentIdAndJobId(stu.getId(), JOB_ID)).thenReturn(Optional.empty());

        var result = engine.computeForJob(JOB_ID);

        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.newlyMatched()).isEqualTo(1);
        assertThat(result.notificationsDispatched()).isEqualTo(1);
        verify(eligibleJobRepository).save(any(EligibleJob.class));
        verify(notificationService).notify(eq(stu.getUser().getId()),
                eq(NotificationType.NEW_ELIGIBLE_JOB), anyString(), anyString(), anyString());
        verify(emailService).sendNewEligibleJob(eq("a@mit.edu"), anyString(), anyString(), anyString(), anyString());
        verify(jobRepository).save(job);
        assertThat(job.getEligibilityComputedAt()).isNotNull();
    }

    @Test
    void computeForJob_idempotentWhenAlreadyEligible() {
        Job job = approvedJob();
        Student stu = student("a@mit.edu");
        EligibleJob existing = EligibleJob.builder()
                .id(UUID.randomUUID()).student(stu).job(job).status(EligibleJobStatus.ELIGIBLE).build();
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(stu));
        when(eligibleJobRepository.findByStudentIdAndJobId(stu.getId(), JOB_ID))
                .thenReturn(Optional.of(existing));

        var result = engine.computeForJob(JOB_ID);

        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.newlyMatched()).isZero();
        // No new row saved, no re-notification.
        verify(eligibleJobRepository, never()).save(any(EligibleJob.class));
        verify(notificationService, never()).notify(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void computeForJob_reactivatesRevokedRow() {
        Job job = approvedJob();
        Student stu = student("a@mit.edu");
        EligibleJob revoked = EligibleJob.builder()
                .id(UUID.randomUUID()).student(stu).job(job).status(EligibleJobStatus.REVOKED).build();
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(stu));
        when(eligibleJobRepository.findByStudentIdAndJobId(stu.getId(), JOB_ID))
                .thenReturn(Optional.of(revoked));

        var result = engine.computeForJob(JOB_ID);

        assertThat(revoked.getStatus()).isEqualTo(EligibleJobStatus.ELIGIBLE);
        assertThat(result.newlyMatched()).isEqualTo(1);
        verify(eligibleJobRepository).save(revoked);
        verify(notificationService).notify(any(), eq(NotificationType.NEW_ELIGIBLE_JOB),
                anyString(), anyString(), anyString());
    }

    @Test
    void computeForJob_revokesRowWhenNoLongerEligible() {
        Job job = approvedJob();
        // Add a CGPA rule the student fails.
        job.getEligibility().setMinCgpa(new java.math.BigDecimal("9.5"));
        Student stu = student("a@mit.edu"); // cgpa 9.0 < 9.5
        EligibleJob existing = EligibleJob.builder()
                .id(UUID.randomUUID()).student(stu).job(job).status(EligibleJobStatus.ELIGIBLE).build();
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(stu));
        when(eligibleJobRepository.findByStudentIdAndJobId(stu.getId(), JOB_ID))
                .thenReturn(Optional.of(existing));

        var result = engine.computeForJob(JOB_ID);

        assertThat(result.matched()).isZero();
        assertThat(result.revoked()).isEqualTo(1);
        assertThat(existing.getStatus()).isEqualTo(EligibleJobStatus.REVOKED);
    }

    @Test
    void computeForJob_preservesAppliedRowWhenNoLongerEligible() {
        Job job = approvedJob();
        job.getEligibility().setMinCgpa(new java.math.BigDecimal("9.5"));
        Student stu = student("a@mit.edu");
        EligibleJob applied = EligibleJob.builder()
                .id(UUID.randomUUID()).student(stu).job(job).status(EligibleJobStatus.APPLIED).build();
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(stu));
        when(eligibleJobRepository.findByStudentIdAndJobId(stu.getId(), JOB_ID))
                .thenReturn(Optional.of(applied));

        var result = engine.computeForJob(JOB_ID);

        // APPLIED rows are never revoked — application trail is preserved.
        assertThat(applied.getStatus()).isEqualTo(EligibleJobStatus.APPLIED);
        assertThat(result.revoked()).isZero();
        verify(eligibleJobRepository, never()).save(any(EligibleJob.class));
    }

    @Test
    void computeForJob_mixedCandidatePool() {
        Job job = approvedJob();
        job.getEligibility().setMinCgpa(new java.math.BigDecimal("8.0"));
        Student pass = student("pass@mit.edu"); // 9.0 passes
        Student fail = student("fail@mit.edu");
        fail.setCgpa(new java.math.BigDecimal("6.0")); // fails
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(studentRepository.findEligibilityCandidates(UNIVERSITY_ID)).thenReturn(List.of(pass, fail));
        when(eligibleJobRepository.findByStudentIdAndJobId(any(), eq(JOB_ID))).thenReturn(Optional.empty());

        var result = engine.computeForJob(JOB_ID);

        assertThat(result.candidatesEvaluated()).isEqualTo(2);
        assertThat(result.matched()).isEqualTo(1);
        assertThat(result.newlyMatched()).isEqualTo(1);
        verify(eligibleJobRepository, times(1)).save(any(EligibleJob.class));
    }
}
