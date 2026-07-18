package com.campusconnect.portal.service.job;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.JobType;
import com.campusconnect.portal.dto.dashboard.CompanyDashboardResponse;
import com.campusconnect.portal.dto.job.CreateJobRequest;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.JobEligibility;
import com.campusconnect.portal.entity.PlacementCell;
import com.campusconnect.portal.entity.University;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.mapper.JobMapper;
import com.campusconnect.portal.repository.ApplicationRepository;
import com.campusconnect.portal.repository.CompanyRepository;
import com.campusconnect.portal.repository.EligibleJobRepository;
import com.campusconnect.portal.repository.JobRepository;
import com.campusconnect.portal.repository.PlacementCellRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the company job lifecycle: creation into PENDING, PENDING-only edit/delete gating,
 * ownership scoping, and dashboard aggregation.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock private JobRepository jobRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private PlacementCellRepository placementCellRepository;
    @Mock private EligibleJobRepository eligibleJobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobMapper jobMapper;

    @InjectMocks private JobServiceImpl service;

    private static final UUID COMPANY_USER_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID OTHER_COMPANY_ID = UUID.randomUUID();
    private static final UUID UNIVERSITY_ID = UUID.randomUUID();
    private static final UUID JOB_ID = UUID.randomUUID();

    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(COMPANY_ID).name("Acme")
                .status(ApprovalStatus.APPROVED).build();
    }

    private CreateJobRequest createRequest() {
        return new CreateJobRequest(UNIVERSITY_ID, "SWE", "Great role", JobType.FULL_TIME,
                "Bengaluru", false, null, null, "INR", 3,
                Instant.now().plusSeconds(86400), null);
    }

    private Job ownedJob(JobStatus status) {
        Job job = Job.builder().id(JOB_ID).status(status).company(company).build();
        job.setEligibility(JobEligibility.builder().build());
        return job;
    }

    // ---------------------------------------------------------------- create

    @Test
    void createJob_persistsAsPending() {
        University uni = University.builder().id(UNIVERSITY_ID).build();
        PlacementCell cell = PlacementCell.builder().id(UUID.randomUUID()).university(uni).build();
        Job entity = ownedJob(JobStatus.PENDING);
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(placementCellRepository.findByUniversityId(UNIVERSITY_ID)).thenReturn(Optional.of(cell));
        when(jobMapper.toEntity(any())).thenReturn(entity);
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createJob(COMPANY_USER_ID, createRequest());

        assertThat(entity.getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(entity.getCompany()).isEqualTo(company);
        assertThat(entity.getPlacementCell()).isEqualTo(cell);
        verify(jobRepository).save(entity);
    }

    @Test
    void createJob_rejectsUnapprovedCompany() {
        company.setStatus(ApprovalStatus.PENDING);
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> service.createJob(COMPANY_USER_ID, createRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_NOT_APPROVED);
        verify(jobRepository, never()).save(any());
    }

    @Test
    void createJob_failsWhenUniversityHasNoCell() {
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(placementCellRepository.findByUniversityId(UNIVERSITY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createJob(COMPANY_USER_ID, createRequest()))
                .isInstanceOf(BusinessRuleException.class);
        verify(jobRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- delete

    @Test
    void deleteJob_removesPendingJob() {
        Job job = ownedJob(JobStatus.PENDING);
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        service.deleteJob(COMPANY_USER_ID, JOB_ID);

        verify(jobRepository).delete(job);
    }

    @Test
    void deleteJob_rejectsApprovedJob() {
        Job job = ownedJob(JobStatus.APPROVED);
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.deleteJob(COMPANY_USER_ID, JOB_ID))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_STATE_TRANSITION);
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void deleteJob_deniesJobOwnedByAnotherCompany() {
        Company other = Company.builder().id(OTHER_COMPANY_ID).build();
        Job job = Job.builder().id(JOB_ID).status(JobStatus.PENDING).company(other).build();
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(jobRepository.findDetailById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.deleteJob(COMPANY_USER_ID, JOB_ID))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    // ---------------------------------------------------------------- dashboard

    @Test
    void getCompanyDashboard_aggregatesCountsAndApplicationsByStatus() {
        when(companyRepository.findByUserId(COMPANY_USER_ID)).thenReturn(Optional.of(company));
        when(jobRepository.countByCompanyId(COMPANY_ID)).thenReturn(12L);
        when(jobRepository.countByCompanyIdAndStatus(COMPANY_ID, JobStatus.PENDING)).thenReturn(2L);
        when(jobRepository.countByCompanyIdAndStatus(COMPANY_ID, JobStatus.APPROVED)).thenReturn(7L);
        when(jobRepository.countByCompanyIdAndStatus(COMPANY_ID, JobStatus.REJECTED)).thenReturn(2L);
        when(jobRepository.countByCompanyIdAndStatus(COMPANY_ID, JobStatus.CLOSED)).thenReturn(1L);
        when(applicationRepository.countByStatusForCompany(COMPANY_ID)).thenReturn(List.of(
                new Object[]{com.campusconnect.portal.common.enums.ApplicationStatus.APPLIED, 5L},
                new Object[]{com.campusconnect.portal.common.enums.ApplicationStatus.SHORTLISTED, 3L}));

        CompanyDashboardResponse dash = service.getCompanyDashboard(COMPANY_USER_ID);

        assertThat(dash.totalJobs()).isEqualTo(12L);
        assertThat(dash.pendingJobs()).isEqualTo(2L);
        assertThat(dash.approvedJobs()).isEqualTo(7L);
        assertThat(dash.closedJobs()).isEqualTo(1L);
        assertThat(dash.totalApplications()).isEqualTo(8L);
        assertThat(dash.applicationsByStatus()).containsEntry("APPLIED", 5L).containsEntry("SHORTLISTED", 3L);
    }
}
