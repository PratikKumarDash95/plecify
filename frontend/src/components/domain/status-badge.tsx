import { Badge, type BadgeProps } from "@/components/ui/badge";
import {
  applicationStatusLabels,
  jobStatusLabels,
} from "@/lib/format";
import type { ApplicationStatus, JobStatus } from "@/types/domain";

type BadgeVariant = NonNullable<BadgeProps["variant"]>;

const jobStatusVariant: Record<JobStatus, BadgeVariant> = {
  DRAFT: "neutral",
  PENDING: "warning",
  APPROVED: "success",
  REJECTED: "danger",
  CLOSED: "neutral",
  EXPIRED: "neutral",
};

const applicationStatusVariant: Record<ApplicationStatus, BadgeVariant> = {
  APPLIED: "info",
  SHORTLISTED: "primary",
  INTERVIEW_SCHEDULED: "tertiary",
  SELECTED: "success",
  OFFER_RELEASED: "success",
  REJECTED: "danger",
  WITHDRAWN: "neutral",
};

export function JobStatusBadge({ status }: { status: JobStatus }) {
  return <Badge variant={jobStatusVariant[status]}>{jobStatusLabels[status]}</Badge>;
}

export function ApplicationStatusBadge({ status }: { status: ApplicationStatus }) {
  return <Badge variant={applicationStatusVariant[status]}>{applicationStatusLabels[status]}</Badge>;
}
