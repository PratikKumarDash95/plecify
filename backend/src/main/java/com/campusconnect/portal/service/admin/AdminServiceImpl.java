package com.campusconnect.portal.service.admin;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.enums.NotificationType;
import com.campusconnect.portal.dto.admin.CompanyResponse;
import com.campusconnect.portal.dto.admin.CompanySummaryResponse;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.BusinessRuleException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.repository.CompanyRepository;
import com.campusconnect.portal.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Default {@link AdminService}. Owns the company onboarding lifecycle
 * ({@code PENDING → APPROVED | REJECTED}) and notifies the company's account owner of the
 * outcome in-app. Approval is what unblocks {@code JobServiceImpl#createJob}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<CompanySummaryResponse> listCompanies(ApprovalStatus status, Pageable pageable) {
        return companyRepository.findByStatus(status, pageable).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompany(UUID companyId) {
        return toResponse(requireCompany(companyId));
    }

    @Override
    @Transactional
    public CompanyResponse approveCompany(UUID companyId) {
        Company company = requireCompany(companyId);
        if (company.getStatus() == ApprovalStatus.APPROVED) {
            return toResponse(company); // idempotent
        }
        if (company.getStatus() == ApprovalStatus.REJECTED) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "A rejected company cannot be approved; the company must re-register.");
        }
        company.setStatus(ApprovalStatus.APPROVED);
        companyRepository.save(company);
        notifyCompany(company, "Company approved",
                "Your company has been approved. You can now post jobs.");
        log.info("Admin approved company {}", companyId);
        return toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse rejectCompany(UUID companyId, String reason) {
        Company company = requireCompany(companyId);
        if (company.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleException(ErrorCode.ILLEGAL_STATE_TRANSITION,
                    "Only pending companies can be rejected; this company is " + company.getStatus());
        }
        company.setStatus(ApprovalStatus.REJECTED);
        companyRepository.save(company);
        notifyCompany(company, "Company registration not approved", "Reason: " + reason);
        log.info("Admin rejected company {}", companyId);
        return toResponse(company);
    }

    // ---------------------------------------------------------------- internals

    private Company requireCompany(UUID companyId) {
        return companyRepository.findWithUserById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
    }

    private void notifyCompany(Company company, String title, String body) {
        notificationService.notify(company.getUser().getId(), NotificationType.SYSTEM_ANNOUNCEMENT,
                title, body, "/company");
    }

    private CompanySummaryResponse toSummary(Company c) {
        User user = c.getUser();
        return new CompanySummaryResponse(
                c.getId(),
                c.getName(),
                c.getIndustry(),
                user != null ? user.getFullName() : null,
                c.getContactEmail(),
                c.getStatus().name(),
                c.getCreatedAt());
    }

    private CompanyResponse toResponse(Company c) {
        User user = c.getUser();
        return new CompanyResponse(
                c.getId(),
                c.getName(),
                c.getIndustry(),
                c.getWebsite(),
                c.getDescription(),
                c.getLogoUrl(),
                c.getHeadquarters(),
                user != null ? user.getFullName() : null,
                c.getContactEmail(),
                c.getContactPhone(),
                user != null ? user.getEmail() : null,
                c.getStatus().name(),
                c.getCreatedAt());
    }
}
