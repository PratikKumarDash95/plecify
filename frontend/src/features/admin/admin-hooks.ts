import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminService } from "@/services/admin-service";
import { queryKeys } from "@/lib/query-keys";
import type { PageParams } from "@/types/api";
import type { ApprovalStatus, RejectCompanyRequest } from "@/types/domain";

export function useAdminCompanies(status: ApprovalStatus, params: PageParams) {
  return useQuery({
    queryKey: queryKeys.admin.companies(status, params),
    queryFn: () => adminService.listCompanies(status, params),
  });
}

export function useAdminCompany(companyId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.admin.company(companyId ?? ""),
    queryFn: () => adminService.getCompany(companyId as string),
    enabled: !!companyId,
  });
}

export function useApproveCompany() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (companyId: string) => adminService.approve(companyId),
    onSuccess: (_data, companyId) => {
      qc.invalidateQueries({ queryKey: ["admin", "companies"] });
      qc.invalidateQueries({ queryKey: queryKeys.admin.company(companyId) });
    },
  });
}

export function useRejectCompany() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ companyId, payload }: { companyId: string; payload: RejectCompanyRequest }) =>
      adminService.reject(companyId, payload),
    onSuccess: (_data, { companyId }) => {
      qc.invalidateQueries({ queryKey: ["admin", "companies"] });
      qc.invalidateQueries({ queryKey: queryKeys.admin.company(companyId) });
    },
  });
}
