import type { PageParams } from "@/types/api";

/** Centralised react-query key factory to keep cache invalidation consistent. */
export const queryKeys = {
  company: {
    dashboard: ["company", "dashboard"] as const,
    jobs: (params?: PageParams) => ["company", "jobs", params ?? {}] as const,
    job: (jobId: string) => ["company", "job", jobId] as const,
  },
  student: {
    dashboard: ["student", "dashboard"] as const,
    eligibleJobs: (params?: PageParams) => ["student", "eligible-jobs", params ?? {}] as const,
    applications: (params?: PageParams) => ["student", "applications", params ?? {}] as const,
  },
  placement: {
    dashboard: ["placement", "dashboard"] as const,
    pending: (params?: PageParams) => ["placement", "pending", params ?? {}] as const,
    job: (jobId: string) => ["placement", "job", jobId] as const,
  },
  admin: {
    companies: (status: string, params?: PageParams) =>
      ["admin", "companies", status, params ?? {}] as const,
    company: (companyId: string) => ["admin", "company", companyId] as const,
  },
} as const;
