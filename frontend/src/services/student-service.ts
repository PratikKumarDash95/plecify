import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse, PagedResponse, PageParams } from "@/types/api";
import type {
  ApplicationResponse,
  ApplyJobRequest,
  EligibleJobResponse,
  StudentDashboardResponse,
} from "@/types/domain";

/** Student-facing endpoints under /api/v1/student. */
export const studentService = {
  async eligibleJobs(params: PageParams = {}): Promise<PagedResponse<EligibleJobResponse>> {
    const { data } = await apiClient.get<ApiResponse<PagedResponse<EligibleJobResponse>>>(
      "/student/eligible-jobs",
      { params },
    );
    return unwrap(data);
  },

  async apply(jobId: string, payload: ApplyJobRequest): Promise<ApplicationResponse> {
    const { data } = await apiClient.post<ApiResponse<ApplicationResponse>>(
      `/student/jobs/${jobId}/apply`,
      payload,
    );
    return unwrap(data);
  },

  async applications(params: PageParams = {}): Promise<PagedResponse<ApplicationResponse>> {
    const { data } = await apiClient.get<ApiResponse<PagedResponse<ApplicationResponse>>>(
      "/student/applications",
      { params },
    );
    return unwrap(data);
  },

  async dashboard(): Promise<StudentDashboardResponse> {
    const { data } = await apiClient.get<ApiResponse<StudentDashboardResponse>>("/student/dashboard");
    return unwrap(data);
  },
};
