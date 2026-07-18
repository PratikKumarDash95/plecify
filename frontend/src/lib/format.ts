import type {
  ApplicationStatus,
  ApprovalStatus,
  JobStatus,
  JobType,
  WorkAuthorization,
} from "@/types/domain";

/** Human-readable labels for backend enums (which are SCREAMING_SNAKE_CASE on the wire). */
export const jobTypeLabels: Record<JobType, string> = {
  FULL_TIME: "Full-time",
  INTERNSHIP: "Internship",
  INTERNSHIP_PLUS_PPO: "Internship + PPO",
  PART_TIME: "Part-time",
  CONTRACT: "Contract",
};

export const jobStatusLabels: Record<JobStatus, string> = {
  DRAFT: "Draft",
  PENDING: "Pending review",
  APPROVED: "Approved",
  REJECTED: "Rejected",
  CLOSED: "Closed",
  EXPIRED: "Expired",
};

export const approvalStatusLabels: Record<ApprovalStatus, string> = {
  PENDING: "Pending review",
  APPROVED: "Approved",
  REJECTED: "Rejected",
};

export const applicationStatusLabels: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  SHORTLISTED: "Shortlisted",
  INTERVIEW_SCHEDULED: "Interview scheduled",
  SELECTED: "Selected",
  OFFER_RELEASED: "Offer released",
  REJECTED: "Rejected",
  WITHDRAWN: "Withdrawn",
};

export const workAuthorizationLabels: Record<WorkAuthorization, string> = {
  CITIZEN: "Citizen",
  PERMANENT_RESIDENT: "Permanent resident",
  REQUIRES_SPONSORSHIP: "Requires sponsorship",
  ANY: "Any",
};

/** Formats a salary range using the job's currency. Returns "Not disclosed" when absent. */
export function formatSalaryRange(
  min?: number,
  max?: number,
  currency = "INR",
): string {
  if (min == null && max == null) return "Not disclosed";
  const fmt = (n: number) => {
    try {
      return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency,
        maximumFractionDigits: 0,
        notation: n >= 100_000 ? "compact" : "standard",
      }).format(n);
    } catch {
      // Unknown currency code — fall back to a plain number with the code appended.
      return `${currency} ${n.toLocaleString()}`;
    }
  };
  if (min != null && max != null) return `${fmt(min)} – ${fmt(max)}`;
  return fmt((min ?? max) as number);
}

const dateFmt = new Intl.DateTimeFormat("en-US", {
  year: "numeric",
  month: "short",
  day: "numeric",
});

const dateTimeFmt = new Intl.DateTimeFormat("en-US", {
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "numeric",
  minute: "2-digit",
});

export function formatDate(iso?: string): string {
  if (!iso) return "—";
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? "—" : dateFmt.format(d);
}

export function formatDateTime(iso?: string): string {
  if (!iso) return "—";
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? "—" : dateTimeFmt.format(d);
}

/** Relative deadline text, e.g. "in 3 days", "Today", "2 days ago". */
export function formatDeadline(iso?: string): { text: string; urgent: boolean; past: boolean } {
  if (!iso) return { text: "No deadline", urgent: false, past: false };
  const target = new Date(iso).getTime();
  if (Number.isNaN(target)) return { text: "—", urgent: false, past: false };
  const diffMs = target - Date.now();
  const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));
  if (diffMs < 0) return { text: "Deadline passed", urgent: false, past: true };
  if (diffDays === 0) return { text: "Due today", urgent: true, past: false };
  if (diffDays === 1) return { text: "Due tomorrow", urgent: true, past: false };
  if (diffDays <= 5) return { text: `Due in ${diffDays} days`, urgent: true, past: false };
  return { text: `Due ${formatDate(iso)}`, urgent: false, past: false };
}
