import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { placementService } from "@/services/placement-service";
import { queryKeys } from "@/lib/query-keys";
import type { PageParams } from "@/types/api";
import type { RejectJobRequest } from "@/types/domain";

export function usePlacementDashboard() {
  return useQuery({
    queryKey: queryKeys.placement.dashboard,
    queryFn: () => placementService.dashboard(),
  });
}

export function usePendingJobs(params: PageParams) {
  return useQuery({
    queryKey: queryKeys.placement.pending(params),
    queryFn: () => placementService.pending(params),
  });
}

export function usePlacementJob(jobId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.placement.job(jobId ?? ""),
    queryFn: () => placementService.get(jobId as string),
    enabled: !!jobId,
  });
}

export function useApproveJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (jobId: string) => placementService.approve(jobId),
    onSuccess: (_data, jobId) => {
      qc.invalidateQueries({ queryKey: ["placement", "pending"] });
      qc.invalidateQueries({ queryKey: queryKeys.placement.dashboard });
      qc.invalidateQueries({ queryKey: queryKeys.placement.job(jobId) });
    },
  });
}

export function useRejectJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ jobId, payload }: { jobId: string; payload: RejectJobRequest }) =>
      placementService.reject(jobId, payload),
    onSuccess: (_data, { jobId }) => {
      qc.invalidateQueries({ queryKey: ["placement", "pending"] });
      qc.invalidateQueries({ queryKey: queryKeys.placement.dashboard });
      qc.invalidateQueries({ queryKey: queryKeys.placement.job(jobId) });
    },
  });
}
