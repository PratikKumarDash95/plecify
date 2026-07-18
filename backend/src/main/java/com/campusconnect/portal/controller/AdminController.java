package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.common.response.PagedResponse;
import com.campusconnect.portal.dto.admin.CompanyResponse;
import com.campusconnect.portal.dto.admin.CompanySummaryResponse;
import com.campusconnect.portal.dto.admin.RejectCompanyRequest;
import com.campusconnect.portal.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Platform-administration endpoints. Restricted to the {@code ADMIN} role. Currently exposes
 * the company-onboarding review workflow that company registration promises but no other role
 * can perform.
 */
@RestController
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Company onboarding review (approve/reject)")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List companies by status",
            description = "Paginated companies filtered by approval status (defaults to PENDING).")
    @GetMapping
    public ApiResponse<PagedResponse<CompanySummaryResponse>> listCompanies(
            @RequestParam(defaultValue = "PENDING") ApprovalStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CompanySummaryResponse> page = adminService.listCompanies(status, pageable);
        return ApiResponse.success(PagedResponse.of(page));
    }

    @Operation(summary = "Get a company for review",
            description = "Full profile for a single company registration.")
    @GetMapping("/{companyId}")
    public ApiResponse<CompanyResponse> getCompany(@PathVariable UUID companyId) {
        return ApiResponse.success(adminService.getCompany(companyId));
    }

    @Operation(summary = "Approve a company",
            description = "Approves a pending company so it can post jobs. Idempotent for an "
                    + "already-approved company.")
    @PostMapping("/{companyId}/approve")
    public ApiResponse<CompanyResponse> approveCompany(@PathVariable UUID companyId) {
        return ApiResponse.success(adminService.approveCompany(companyId), "Company approved");
    }

    @Operation(summary = "Reject a company",
            description = "Rejects a pending company with a mandatory reason surfaced to the company.")
    @PostMapping("/{companyId}/reject")
    public ApiResponse<CompanyResponse> rejectCompany(@PathVariable UUID companyId,
                                                      @Valid @RequestBody RejectCompanyRequest request) {
        return ApiResponse.success(adminService.rejectCompany(companyId, request.reason()), "Company rejected");
    }
}
