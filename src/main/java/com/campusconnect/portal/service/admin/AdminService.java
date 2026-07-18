package com.campusconnect.portal.service.admin;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.dto.admin.CompanyResponse;
import com.campusconnect.portal.dto.admin.CompanySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Platform-administration operations. Restricted to the {@code ADMIN} role at the controller
 * layer. Currently scoped to company onboarding: reviewing, approving, and rejecting the
 * companies that register through {@code /auth/register/company} in {@code PENDING} status.
 */
public interface AdminService {

    /** Paginated companies filtered by approval status. */
    Page<CompanySummaryResponse> listCompanies(ApprovalStatus status, Pageable pageable);

    /** Full detail for a single company. */
    CompanyResponse getCompany(UUID companyId);

    /**
     * Approves a company so it can post jobs. No-op-safe on an already-approved company;
     * rejects the transition from a terminal state otherwise.
     */
    CompanyResponse approveCompany(UUID companyId);

    /** Rejects a pending company with a reason surfaced to the company. */
    CompanyResponse rejectCompany(UUID companyId, String reason);
}
