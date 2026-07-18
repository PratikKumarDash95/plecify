import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse, PagedResponse, PageParams } from "@/types/api";
import type {
  CompanyDashboardResponse,
  CreateJobRequest,
  JobResponse,
  JobSummaryResponse,
  UpdateJobRequest,
} from "@/types/domain";

/** Company-facing job endpoints under /api/v1/company/jobs. */
export const jobService = {
  async list(params: PageParams = {}): Promise<PagedResponse<JobSummaryResponse>> {
    const { data } = await apiClient.get<ApiResponse<PagedResponse<JobSummaryResponse>>>(
      "/company/jobs",
      { params },
    );
    return unwrap(data);
  },

  async get(jobId: string): Promise<JobResponse> {
    const { data } = await apiClient.get<ApiResponse<JobResponse>>(`/company/jobs/${jobId}`);
    return unwrap(data);
  },

  async create(payload: CreateJobRequest): Promise<JobResponse> {
    const { data } = await apiClient.post<ApiResponse<JobResponse>>("/company/jobs", payload);
    return unwrap(data);
  },

  async update(jobId: string, payload: UpdateJobRequest): Promise<JobResponse> {
    const { data } = await apiClient.put<ApiResponse<JobResponse>>(`/company/jobs/${jobId}`, payload);
    return unwrap(data);
  },

  async remove(jobId: string): Promise<string> {
    const { data } = await apiClient.delete<ApiResponse<void>>(`/company/jobs/${jobId}`);
    return data.message;
  },

  async dashboard(): Promise<CompanyDashboardResponse> {
    const { data } = await apiClient.get<ApiResponse<CompanyDashboardResponse>>(
      "/company/jobs/dashboard",
    );
    return unwrap(data);
  },
};
