package com.campusconnect.portal.service.student;

import com.campusconnect.portal.common.enums.ApplicationStatus;
import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.dashboard.StudentDashboardResponse;
import com.campusconnect.portal.dto.student.ApplyJobRequest;
import com.campusconnect.portal.entity.Application;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.EligibleJob;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.Student;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.mapper.ApplicationMapper;
import com.campusconnect.portal.mapper.JobMapper;
import com.campusconnect.portal.repository.ApplicationRepository;
import com.campusconnect.portal.repository.ApplicationStatusHistoryRepository;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.service.email.EmailService;
import com.campusconnect.portal.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the student application path: eligibility gating (via the pre-computed row), the
 * open-for-applications window, duplicate protection, the ELIGIBLE→APPLIED flip, and
 * dashboard metrics read straight from the materialised table.
 */
@ExtendWith(MockitoExtension.class)
class StudentJobServiceImplTest {

    @Mock private StudentRepository studentRepository;
    @Mock private EligibleJobRepository eligibleJobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationStatusHistoryRepository statusHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private JobMapper jobMapper;
    @Mock private ApplicationMapper applicationMapper;

    @InjectMocks private StudentJobServiceImpl service;

    private static final UUID STUDENT_USER_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID JOB_ID = UUID.randomUUID();

    private Student student;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(UUID.randomUUID()).email("stu@mit.edu").fullName("Stu").build();
        student = Student.builder().id(STUDENT_ID).user(user).placementEligible(true).build();
    }

    private Job openJob() {
        User companyUser = User.builder().id(UUID.randomUUID()).email("hr@acme.io").fullName("HR").build();
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme").user(companyUser).build();
        return Job.builder()
                .id(JOB_ID)
                .status(JobStatus.APPROVED)
                .title("SWE")
                .company(company)
                .applicationDeadline(Instant.now().plusSeconds(86400))
                .build();
    }

    private EligibleJob eligibleRow(EligibleJobStatus status, Job job) {
        return EligibleJob.builder().id(UUID.randomUUID()).student(student).job(job).status(status).build();
    }

    // ---------------------------------------------------------------- apply

    @Test
    void applyToJob_succeedsForEligibleOpenJob() {
        Job job = openJob();
        EligibleJob row = eligibleRow(EligibleJobStatus.ELIGIBLE, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));
        when(applicationRepository.existsByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, "Please hire me"));

        assertThat(row.getStatus()).isEqualTo(EligibleJobStatus.APPLIED);
        verify(applicationRepository).save(any(Application.class));
        verify(statusHistoryRepository).save(any());
        verify(eligibleJobRepository).save(row);
        // Both student confirmation and company alert fire.
        verify(notificationService).notify(eq(student.getUser().getId()),
                eq(NotificationType.APPLICATION_SUBMITTED), anyString(), anyString(), anyString());
        verify(notificationService).notify(eq(job.getCompany().getUser().getId()),
                eq(NotificationType.APPLICATION_RECEIVED), anyString(), anyString(), anyString());
    }

    @Test
    void applyToJob_failsWhenNoEligibleRow() {
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_ELIGIBLE);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_failsWhenAlreadyApplied() {
        Job job = openJob();
        EligibleJob row = eligibleRow(EligibleJobStatus.APPLIED, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_APPLICATION);
    }

    @Test
    void applyToJob_failsWhenEligibilityRevoked() {
        Job job = openJob();
        EligibleJob row = eligibleRow(EligibleJobStatus.REVOKED, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_ELIGIBLE);
    }

    @Test
    void applyToJob_failsWhenJobClosed() {
        Job job = openJob();
        job.setStatus(JobStatus.CLOSED);
        EligibleJob row = eligibleRow(EligibleJobStatus.ELIGIBLE, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_STATE_TRANSITION);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_failsWhenDeadlinePassed() {
        Job job = openJob();
        job.setApplicationDeadline(Instant.now().minusSeconds(60));
        EligibleJob row = eligibleRow(EligibleJobStatus.ELIGIBLE, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_failsOnRaceDuplicate() {
        Job job = openJob();
        EligibleJob row = eligibleRow(EligibleJobStatus.ELIGIBLE, job);
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.findByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(Optional.of(row));
        when(applicationRepository.existsByStudentIdAndJobId(STUDENT_ID, JOB_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.applyToJob(STUDENT_USER_ID, JOB_ID, new ApplyJobRequest(null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_APPLICATION);
        verify(applicationRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- dashboard

    @Test
    void getDashboard_readsPreComputedCounts() {
        when(studentRepository.findByUserId(STUDENT_USER_ID)).thenReturn(Optional.of(student));
        when(eligibleJobRepository.countByStudentIdAndStatus(STUDENT_ID, EligibleJobStatus.ELIGIBLE)).thenReturn(6L);
        when(applicationRepository.countByStudentId(STUDENT_ID)).thenReturn(3L);
        when(applicationRepository.countByStudentIdAndStatus(STUDENT_ID, ApplicationStatus.INTERVIEW_SCHEDULED))
                .thenReturn(1L);
        when(eligibleJobRepository.findUpcomingDeadlines(eq(STUDENT_ID), any(), any()))
                .thenReturn(java.util.List.of());

        StudentDashboardResponse dash = service.getDashboard(STUDENT_USER_ID);

        assertThat(dash.placementEligible()).isTrue();
        assertThat(dash.eligibleJobs()).isEqualTo(6L);
        assertThat(dash.appliedJobs()).isEqualTo(3L);
        assertThat(dash.interviewCount()).isEqualTo(1L);
        assertThat(dash.upcomingDeadlines()).isEmpty();
    }
}
