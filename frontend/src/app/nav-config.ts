import { paths } from "./routes";
import type { Role } from "@/types/auth";

export interface NavItem {
  label: string;
  to: string;
  icon: string;
  /** Match child routes too (e.g. /company/jobs/:id under "Jobs"). */
  matchPrefix?: boolean;
}

// Only routes with real backing endpoints are included. The Stitch mockups show extra items
// (Applicants, Companies, Interviews, Reports) that have no controller, so they are omitted
// rather than wired to nothing.
export const navByRole: Record<Role, NavItem[]> = {
  STUDENT: [
    { label: "Overview", to: paths.studentDashboard, icon: "dashboard" },
    { label: "Eligible Jobs", to: paths.studentJobs, icon: "work", matchPrefix: true },
    { label: "My Applications", to: paths.studentApplications, icon: "assignment", matchPrefix: true },
  ],
  COMPANY: [
    { label: "Overview", to: paths.companyDashboard, icon: "dashboard" },
    { label: "My Jobs", to: paths.companyJobs, icon: "work", matchPrefix: true },
    { label: "Post a Job", to: paths.companyJobNew, icon: "add_box" },
  ],
  PLACEMENT_CELL: [
    { label: "Overview", to: paths.placementDashboard, icon: "dashboard" },
    { label: "Pending Review", to: paths.placementPending, icon: "pending_actions", matchPrefix: true },
  ],
  ADMIN: [
    { label: "Overview", to: paths.adminDashboard, icon: "dashboard" },
    { label: "Companies", to: paths.adminCompanies, icon: "domain", matchPrefix: true },
  ],
};

export const roleDisplayName: Record<Role, string> = {
  STUDENT: "Student",
  COMPANY: "Recruiter",
  PLACEMENT_CELL: "Placement Cell",
  ADMIN: "Administrator",
};
