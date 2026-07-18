import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse, PagedResponse, PageParams } from "@/types/api";
import type {
  ApprovalStatus,
  CompanyResponse,
  CompanySummaryResponse,
  RejectCompanyRequest,
} from "@/types/domain";

/** Admin company-review endpoints under /api/v1/admin/companies. */
export const adminService = {
  async listCompanies(
    status: ApprovalStatus = "PENDING",
    params: PageParams = {},
  ): Promise<PagedResponse<CompanySummaryResponse>> {
    const { data } = await apiClient.get<ApiResponse<PagedResponse<CompanySummaryResponse>>>(
      "/admin/companies",
      { params: { status, ...params } },
    );
    return unwrap(data);
  },

  async getCompany(companyId: string): Promise<CompanyResponse> {
    const { data } = await apiClient.get<ApiResponse<CompanyResponse>>(
      `/admin/companies/${companyId}`,
    );
    return unwrap(data);
  },

  async approve(companyId: string): Promise<CompanyResponse> {
    const { data } = await apiClient.post<ApiResponse<CompanyResponse>>(
      `/admin/companies/${companyId}/approve`,
    );
    return unwrap(data);
  },

  async reject(companyId: string, payload: RejectCompanyRequest): Promise<CompanyResponse> {
    const { data } = await apiClient.post<ApiResponse<CompanyResponse>>(
      `/admin/companies/${companyId}/reject`,
      payload,
    );
    return unwrap(data);
  },
};
