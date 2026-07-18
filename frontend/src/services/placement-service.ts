import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse, PagedResponse, PageParams } from "@/types/api";
import type {
  ApproveJobResponse,
  JobResponse,
  JobSummaryResponse,
  PlacementDashboardResponse,
  RejectJobRequest,
} from "@/types/domain";

/** Placement-cell endpoints under /api/v1/placement/jobs. */
export const placementService = {
  async pending(params: PageParams = {}): Promise<PagedResponse<JobSummaryResponse>> {
    const { data } = await apiClient.get<ApiResponse<PagedResponse<JobSummaryResponse>>>(
      "/placement/jobs/pending",
      { params },
    );
    return unwrap(data);
  },

  async get(jobId: string): Promise<JobResponse> {
    const { data } = await apiClient.get<ApiResponse<JobResponse>>(`/placement/jobs/${jobId}`);
    return unwrap(data);
  },

  async approve(jobId: string): Promise<ApproveJobResponse> {
    const { data } = await apiClient.post<ApiResponse<ApproveJobResponse>>(
      `/placement/jobs/${jobId}/approve`,
    );
    return unwrap(data);
  },

  async reject(jobId: string, payload: RejectJobRequest): Promise<JobResponse> {
    const { data } = await apiClient.post<ApiResponse<JobResponse>>(
      `/placement/jobs/${jobId}/reject`,
      payload,
    );
    return unwrap(data);
  },

  async dashboard(): Promise<PlacementDashboardResponse> {
    const { data } = await apiClient.get<ApiResponse<PlacementDashboardResponse>>(
      "/placement/jobs/dashboard",
    );
    return unwrap(data);
  },
};
