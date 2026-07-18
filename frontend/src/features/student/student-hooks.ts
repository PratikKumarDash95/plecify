import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { studentService } from "@/services/student-service";
import { queryKeys } from "@/lib/query-keys";
import type { PageParams } from "@/types/api";
import type { ApplyJobRequest } from "@/types/domain";

export function useStudentDashboard() {
  return useQuery({
    queryKey: queryKeys.student.dashboard,
    queryFn: () => studentService.dashboard(),
  });
}

export function useEligibleJobs(params: PageParams) {
  return useQuery({
    queryKey: queryKeys.student.eligibleJobs(params),
    queryFn: () => studentService.eligibleJobs(params),
  });
}

export function useStudentApplications(params: PageParams) {
  return useQuery({
    queryKey: queryKeys.student.applications(params),
    queryFn: () => studentService.applications(params),
  });
}

export function useApplyToJob() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ jobId, payload }: { jobId: string; payload: ApplyJobRequest }) =>
      studentService.apply(jobId, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["student", "eligible-jobs"] });
      qc.invalidateQueries({ queryKey: ["student", "applications"] });
      qc.invalidateQueries({ queryKey: queryKeys.student.dashboard });
    },
  });
}
