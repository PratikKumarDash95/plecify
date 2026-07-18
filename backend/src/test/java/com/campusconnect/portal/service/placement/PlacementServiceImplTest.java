package com.campusconnect.portal.service.placement;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.placement.ApproveJobResponse;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.PlacementCell;
import com.campusconnect.portal.entity.University;
import com.campusconnect.portal.entity.User;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the placement-cell review lifecycle: ownership scoping, PENDING-only transitions,
 * engine invocation on approval, company notifications, and dashboard aggregation.
 */
@ExtendWith(MockitoExtension.class)
class PlacementServiceImplTest {

    @Mock private JobRepository jobRepository;
    @Mock private PlacementCellRepository placementCellRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EligibleJobRepository eligibleJobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private EligibilityEngineService eligibilityEngineService;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;
    @Mock private JobMapper jobMapper;

    @InjectMocks private PlacementServiceImpl service;

    private static final UUID PLACEMENT_USER_ID = UUID.randomUUID();
    private static final UUID CELL_ID = UUID.randomUUID();
    private static final UUID UNIVERSITY_ID = UUID.randomUUID();
    private static final UUID JOB_ID = UUID.randomUUID();

    private PlacementCell cell;

    @BeforeEach
    void setUp() {
        University uni = University.builder().id(UNIVERSITY_ID).name("MIT").build();
        cell = PlacementCell.builder().id(CELL_ID).university(uni).build();
    }

    private Job pendingJob() {
        User companyUser = User.builder().id(UUID.randomUUID()).email("hr@acme.io").fullName("Acme HR").build();
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme").user(companyUser).build();
        return Job.builder()
                .id(JOB_ID)
                .status(JobStatus.PENDING)
                .title("SWE")
                .company(company)
                .placementCell(cell)
                .build();
    }

    // ---------------------------------------------------------------- approve

    @Test
    void approveJob_flipsStatusRunsEngineAndNotifies() {
        Job job = pendingJob();
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(eligibilityEngineService.computeForJob(JOB_ID))
                .thenReturn(new EligibilityRunResult(JOB_ID, 10, 4, 4, 0, 4));

        ApproveJobResponse response = service.approveJob(PLACEMENT_USER_ID, JOB_ID);

        assertThat(job.getStatus()).isEqualTo(JobStatus.APPROVED);
        assertThat(job.getApprovedBy()).isEqualTo(PLACEMENT_USER_ID);
        assertThat(job.getApprovedAt()).isNotNull();
        assertThat(job.getReviewedAt()).isNotNull();
        assertThat(response.eligibleStudentsMatched()).isEqualTo(4);
        assertThat(response.notificationsDispatched()).isEqualTo(4);
        verify(jobRepository).save(job);
        verify(eligibilityEngineService).computeForJob(JOB_ID);
        verify(notificationService).notify(eq(job.getCompany().getUser().getId()),
                eq(NotificationType.JOB_APPROVED), anyString(), anyString(), anyString());
        verify(emailService).sendJobApproved(eq("hr@acme.io"), anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void approveJob_rejectsNonPending() {
        Job job = pendingJob();
        job.setStatus(JobStatus.APPROVED);
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.approveJob(PLACEMENT_USER_ID, JOB_ID))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_STATE_TRANSITION);
        verify(eligibilityEngineService, never()).computeForJob(any());
    }

    @Test
    void approveJob_deniesJobFromAnotherCell() {
        Job job = pendingJob();
        PlacementCell otherCell = PlacementCell.builder().id(UUID.randomUUID()).build();
        job.setPlacementCell(otherCell);
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.approveJob(PLACEMENT_USER_ID, JOB_ID))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    // ---------------------------------------------------------------- reject

    @Test
    void rejectJob_setsReasonAndNotifies() {
        Job job = pendingJob();
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));
        when(eligibleJobRepository.countByJobId(JOB_ID)).thenReturn(0L);
        when(applicationRepository.countByJobId(JOB_ID)).thenReturn(0L);

        service.rejectJob(PLACEMENT_USER_ID, JOB_ID, "Incomplete JD");

        assertThat(job.getStatus()).isEqualTo(JobStatus.REJECTED);
        assertThat(job.getRejectionReason()).isEqualTo("Incomplete JD");
        assertThat(job.getReviewedAt()).isNotNull();
        verify(eligibilityEngineService, never()).computeForJob(any());
        verify(notificationService).notify(eq(job.getCompany().getUser().getId()),
                eq(NotificationType.JOB_REJECTED), anyString(), anyString(), anyString());
        verify(emailService).sendJobRejected(eq("hr@acme.io"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejectJob_rejectsNonPending() {
        Job job = pendingJob();
        job.setStatus(JobStatus.REJECTED);
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.rejectJob(PLACEMENT_USER_ID, JOB_ID, "x"))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ---------------------------------------------------------------- guards

    @Test
    void requireCell_throwsWhenNoCellForUser() {
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.approveJob(PLACEMENT_USER_ID, JOB_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- dashboard

    @Test
    void getDashboard_aggregatesCounts() {
        when(placementCellRepository.findByUserId(PLACEMENT_USER_ID)).thenReturn(Optional.of(cell));
        when(jobRepository.countByPlacementCellIdAndStatus(CELL_ID, JobStatus.PENDING)).thenReturn(3L);
        when(jobRepository.countByPlacementCellIdAndStatus(CELL_ID, JobStatus.APPROVED)).thenReturn(7L);
        when(jobRepository.countByPlacementCellIdAndStatus(CELL_ID, JobStatus.REJECTED)).thenReturn(2L);
        when(jobRepository.countReviewedSince(eq(CELL_ID), eq(JobStatus.APPROVED), any())).thenReturn(1L);
        when(jobRepository.countReviewedSince(eq(CELL_ID), eq(JobStatus.REJECTED), any())).thenReturn(0L);
        when(studentRepository.countByUniversityId(UNIVERSITY_ID)).thenReturn(500L);
        when(studentRepository.countByUniversityIdAndPlacementEligible(UNIVERSITY_ID, true)).thenReturn(420L);
        when(applicationRepository.countByPlacementCellId(CELL_ID)).thenReturn(88L);

        PlacementDashboardResponse dash = service.getDashboard(PLACEMENT_USER_ID);

        assertThat(dash.pendingJobs()).isEqualTo(3L);
        assertThat(dash.approvedJobs()).isEqualTo(7L);
        assertThat(dash.rejectedJobs()).isEqualTo(2L);
        assertThat(dash.approvedToday()).isEqualTo(1L);
        assertThat(dash.rejectedToday()).isZero();
        assertThat(dash.totalStudents()).isEqualTo(500L);
        assertThat(dash.placementEligibleStudents()).isEqualTo(420L);
        assertThat(dash.totalApplications()).isEqualTo(88L);
    }
}
