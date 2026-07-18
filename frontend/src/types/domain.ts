// Domain enums and DTOs mirroring the backend (jobs, applications, eligibility, dashboards).
// Field names and enum members match the Java DTOs/enums exactly.

export type JobType = "FULL_TIME" | "INTERNSHIP" | "INTERNSHIP_PLUS_PPO" | "PART_TIME" | "CONTRACT";

export type JobStatus = "DRAFT" | "PENDING" | "APPROVED" | "REJECTED" | "CLOSED" | "EXPIRED";

/** Company review lifecycle, mirroring com.campusconnect.portal.common.enums.ApprovalStatus. */
export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED";

export type ApplicationStatus =
  | "APPLIED"
  | "SHORTLISTED"
  | "INTERVIEW_SCHEDULED"
  | "SELECTED"
  | "OFFER_RELEASED"
  | "REJECTED"
  | "WITHDRAWN";

export type EligibleJobStatus = "ELIGIBLE" | "APPLIED" | "REVOKED";

export type Gender = "MALE" | "FEMALE" | "OTHER" | "UNDISCLOSED";

export type SkillMatchMode = "ALL" | "ANY";

export type WorkAuthorization = "CITIZEN" | "PERMANENT_RESIDENT" | "REQUIRES_SPONSORSHIP" | "ANY";

export interface JobEligibilityDto {
  minCgpa?: number;
  maxActiveBacklogs?: number;
  maxTotalBacklogs?: number;
  requiredWorkAuthorization?: WorkAuthorization;
  skillMatchMode?: SkillMatchMode;
  departments?: string[];
  branches?: string[];
  passingYears?: number[];
  requiredSkills?: string[];
  allowedLocations?: string[];
  allowedGenders?: Gender[];
  batches?: string[];
  minAge?: number;
  maxAge?: number;
  minPackage?: number;
  maxPackage?: number;
}

export interface JobResponse {
  id: string;
  companyId: string;
  companyName: string;
  companyLogoUrl?: string;
  placementCellId?: string;
  universityId: string;
  universityName?: string;
  title: string;
  description: string;
  jobType: JobType;
  location?: string;
  remoteAllowed: boolean;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  openings: number;
  status: JobStatus;
  applicationDeadline: string;
  rejectionReason?: string;
  reviewedAt?: string;
  approvedBy?: string;
  approvedAt?: string;
  eligibilityComputedAt?: string;
  eligibleStudentCount: number;
  applicationCount: number;
  eligibility?: JobEligibilityDto;
  createdAt: string;
  updatedAt: string;
}

export interface JobSummaryResponse {
  id: string;
  companyId: string;
  companyName: string;
  companyLogoUrl?: string;
  title: string;
  jobType: JobType;
  location?: string;
  remoteAllowed: boolean;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  openings: number;
  status: JobStatus;
  applicationDeadline: string;
  createdAt: string;
}

export interface CreateJobRequest {
  universityId: string;
  title: string;
  description: string;
  jobType: JobType;
  location?: string;
  remoteAllowed: boolean;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  openings?: number;
  applicationDeadline: string;
  eligibility?: JobEligibilityDto;
}

export type UpdateJobRequest = CreateJobRequest;

export interface EligibleJobResponse {
  eligibleJobId: string;
  eligibilityStatus: EligibleJobStatus;
  jobId: string;
  companyId: string;
  companyName: string;
  companyLogoUrl?: string;
  title: string;
  jobType: JobType;
  location?: string;
  remoteAllowed: boolean;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  openings: number;
  applicationDeadline: string;
  matchedAt: string;
}

export interface ApplicationResponse {
  id: string;
  jobId: string;
  jobTitle: string;
  companyId: string;
  companyName: string;
  status: ApplicationStatus;
  resumeUrl?: string;
  coverLetter?: string;
  interviewAt?: string;
  interviewDetails?: string;
  statusNote?: string;
  lastStatusChangeAt?: string;
  createdAt: string;
}

export interface ApplyJobRequest {
  resumeUrl?: string;
  coverLetter?: string;
}

export interface ApproveJobResponse {
  jobId: string;
  status: string;
  approvedBy: string;
  approvedAt: string;
  eligibleStudentsMatched: number;
  notificationsDispatched: number;
}

export interface RejectJobRequest {
  reason: string;
}

// --- dashboards ---------------------------------------------------------------

export interface CompanyDashboardResponse {
  totalJobs: number;
  pendingJobs: number;
  approvedJobs: number;
  rejectedJobs: number;
  closedJobs: number;
  totalApplications: number;
  applicationsByStatus: Record<string, number>;
}

export interface PlacementDashboardResponse {
  pendingJobs: number;
  approvedToday: number;
  rejectedToday: number;
  approvedJobs: number;
  rejectedJobs: number;
  totalStudents: number;
  placementEligibleStudents: number;
  totalApplications: number;
}

export interface StudentDashboardResponse {
  placementEligible: boolean;
  eligibleJobs: number;
  appliedJobs: number;
  interviewCount: number;
  upcomingDeadlines: EligibleJobResponse[];
}

// --- admin --------------------------------------------------------------------

/** Row in the admin company-review list (com.campusconnect.portal.dto.admin.CompanySummaryResponse). */
export interface CompanySummaryResponse {
  id: string;
  name: string;
  industry?: string;
  contactPersonName?: string;
  contactEmail?: string;
  status: ApprovalStatus;
  registeredAt: string;
}

/** Full company profile for admin review (com.campusconnect.portal.dto.admin.CompanyResponse). */
export interface CompanyResponse {
  id: string;
  name: string;
  industry?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
  headquarters?: string;
  contactPersonName?: string;
  contactEmail?: string;
  contactPhone?: string;
  accountEmail?: string;
  status: ApprovalStatus;
  registeredAt: string;
}

export interface RejectCompanyRequest {
  reason: string;
}
