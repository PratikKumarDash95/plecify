import type { Role } from "@/types/auth";

/** Centralised route path constants. Keep in sync with the router in app/router.tsx. */
export const paths = {
  // public
  landing: "/",
  login: "/login",
  registerStudent: "/register/student",
  registerCompany: "/register/company",
  forgotPassword: "/forgot-password",
  resetPassword: "/reset-password",
  verifyEmail: "/verify-email",

  // student
  studentDashboard: "/student",
  studentJobs: "/student/jobs",
  studentApplications: "/student/applications",

  // company
  companyDashboard: "/company",
  companyJobs: "/company/jobs",
  companyJobNew: "/company/jobs/new",
  companyJobDetail: (id = ":jobId") => `/company/jobs/${id}`,
  companyJobEdit: (id = ":jobId") => `/company/jobs/${id}/edit`,

  // placement cell
  placementDashboard: "/placement",
  placementPending: "/placement/pending",
  placementJobDetail: (id = ":jobId") => `/placement/jobs/${id}`,

  // admin
  adminDashboard: "/admin",
  adminCompanies: "/admin/companies",
  adminCompanyDetail: (id = ":companyId") => `/admin/companies/${id}`,
} as const;

/** The landing route for each role after login. */
export const roleHome: Record<Role, string> = {
  STUDENT: paths.studentDashboard,
  COMPANY: paths.companyDashboard,
  PLACEMENT_CELL: paths.placementDashboard,
  ADMIN: paths.adminDashboard,
};
