import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { jobService } from "@/services/job-service";
import { queryKeys } from "@/lib/query-keys";
import type { PageParams } from "@/types/api";
import type { CreateJobRequest, UpdateJobRequest } from "@/types/domain";

export function useCompanyDashboard() {
  return useQuery({
    queryKey: queryKeys.company.dashboard,
    queryFn: () => jobService.dashboard(),
  });
}

export function useCompanyJobs(params: PageParams) {
  return useQuery({
    queryKey: queryKeys.company.jobs(params),
    queryFn: () => jobService.list(params),
  });
}

export function useCompanyJob(jobId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.company.job(jobId ?? ""),
    queryFn: () => jobService.get(jobId as string),
    enabled: !!jobId,
  });
}

export function useCreateJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateJobRequest) => jobService.create(payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["company", "jobs"] });
      qc.invalidateQueries({ queryKey: queryKeys.company.dashboard });
    },
  });
}

export function useUpdateJob(jobId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateJobRequest) => jobService.update(jobId, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["company", "jobs"] });
      qc.invalidateQueries({ queryKey: queryKeys.company.job(jobId) });
    },
  });
}

export function useDeleteJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (jobId: string) => jobService.remove(jobId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["company", "jobs"] });
      qc.invalidateQueries({ queryKey: queryKeys.company.dashboard });
    },
  });
}
