package com.campusconnect.portal.service.admin;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.admin.CompanyResponse;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.repository.CompanyRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the admin company-onboarding lifecycle: approve (with idempotency and terminal-state
 * guarding), reject (PENDING-only), and company-owner notification.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private AdminServiceImpl service;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private Company company;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(USER_ID).email("hr@acme.io").fullName("Acme HR").build();
        company = Company.builder().id(COMPANY_ID).name("Acme").user(user)
                .status(ApprovalStatus.PENDING).build();
    }

    @Test
    void approveCompany_setsApprovedAndNotifies() {
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.of(company));

        CompanyResponse response = service.approveCompany(COMPANY_ID);

        assertThat(company.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.status()).isEqualTo("APPROVED");
        verify(companyRepository).save(company);
        verify(notificationService).notify(eq(USER_ID), eq(NotificationType.SYSTEM_ANNOUNCEMENT),
                any(), any(), any());
    }

    @Test
    void approveCompany_isIdempotentWhenAlreadyApproved() {
        company.setStatus(ApprovalStatus.APPROVED);
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.of(company));

        CompanyResponse response = service.approveCompany(COMPANY_ID);

        assertThat(response.status()).isEqualTo("APPROVED");
        verify(companyRepository, never()).save(any());
        verify(notificationService, never()).notify(any(), any(), any(), any(), any());
    }

    @Test
    void approveCompany_rejectsApprovingARejectedCompany() {
        company.setStatus(ApprovalStatus.REJECTED);
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> service.approveCompany(COMPANY_ID))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_STATE_TRANSITION);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void rejectCompany_setsRejectedAndNotifies() {
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.of(company));

        CompanyResponse response = service.rejectCompany(COMPANY_ID, "Could not verify details");

        assertThat(company.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(response.status()).isEqualTo("REJECTED");
        verify(companyRepository).save(company);
        verify(notificationService).notify(eq(USER_ID), eq(NotificationType.SYSTEM_ANNOUNCEMENT),
                any(), any(), any());
    }

    @Test
    void rejectCompany_rejectsNonPendingCompany() {
        company.setStatus(ApprovalStatus.APPROVED);
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.of(company));

        assertThatThrownBy(() -> service.rejectCompany(COMPANY_ID, "too late"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ILLEGAL_STATE_TRANSITION);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void getCompany_throwsWhenMissing() {
        when(companyRepository.findWithUserById(COMPANY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCompany(COMPANY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
