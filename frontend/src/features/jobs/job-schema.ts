import { z } from "zod";
import type { CreateJobRequest, JobEligibilityDto } from "@/types/domain";

const jobTypes = ["FULL_TIME", "INTERNSHIP", "INTERNSHIP_PLUS_PPO", "PART_TIME", "CONTRACT"] as const;
const genders = ["MALE", "FEMALE", "OTHER", "UNDISCLOSED"] as const;
const skillModes = ["ALL", "ANY"] as const;
const workAuths = ["CITIZEN", "PERMANENT_RESIDENT", "REQUIRES_SPONSORSHIP", "ANY"] as const;

// Optional numeric field that starts as "" in the form; coerces to number or undefined.
const optionalNumber = (opts?: { min?: number; max?: number; int?: boolean }) =>
  z
    .union([z.literal(""), z.coerce.number()])
    .transform((v) => (v === "" ? undefined : v))
    .refine((v) => v === undefined || !opts?.int || Number.isInteger(v), "Must be a whole number")
    .refine((v) => v === undefined || opts?.min === undefined || v >= opts.min, `Min ${opts?.min}`)
    .refine((v) => v === undefined || opts?.max === undefined || v <= opts.max, `Max ${opts?.max}`);

export const jobFormSchema = z
  .object({
    universityId: z.string().min(1, "University is required").uuid("Enter a valid university ID"),
    title: z.string().min(1, "Title is required").max(200),
    description: z.string().min(1, "Description is required").max(20000),
    jobType: z.enum(jobTypes),
    location: z.string().max(150).optional().or(z.literal("")),
    remoteAllowed: z.boolean(),
    salaryMin: optionalNumber({ min: 0 }),
    salaryMax: optionalNumber({ min: 0 }),
    currency: z
      .string()
      .length(3, "3-letter code")
      .optional()
      .or(z.literal("")),
    openings: optionalNumber({ min: 1, max: 100000, int: true }),
    applicationDeadline: z
      .string()
      .min(1, "Deadline is required")
      .refine((v) => new Date(v).getTime() > Date.now(), "Deadline must be in the future"),
    // eligibility (all optional)
    minCgpa: optionalNumber({ min: 0, max: 10 }),
    maxActiveBacklogs: optionalNumber({ min: 0, max: 100, int: true }),
    maxTotalBacklogs: optionalNumber({ min: 0, max: 100, int: true }),
    requiredWorkAuthorization: z.enum(workAuths).optional().or(z.literal("")),
    skillMatchMode: z.enum(skillModes).optional().or(z.literal("")),
    departments: z.string().optional().or(z.literal("")),
    branches: z.string().optional().or(z.literal("")),
    passingYears: z.string().optional().or(z.literal("")),
    requiredSkills: z.string().optional().or(z.literal("")),
    allowedLocations: z.string().optional().or(z.literal("")),
    allowedGenders: z.array(z.enum(genders)).optional(),
    minPackage: optionalNumber({ min: 0 }),
    maxPackage: optionalNumber({ min: 0 }),
  })
  .refine(
    (v) => v.salaryMin === undefined || v.salaryMax === undefined || v.salaryMax >= v.salaryMin,
    { message: "Max salary must be ≥ min salary", path: ["salaryMax"] },
  );

export type JobFormValues = z.input<typeof jobFormSchema>;
export type JobFormParsed = z.output<typeof jobFormSchema>;

/** Splits a comma/newline-separated string into a trimmed, de-duplicated array (or undefined). */
function toList(raw?: string): string[] | undefined {
  if (!raw) return undefined;
  const items = [...new Set(raw.split(/[,\n]/).map((s) => s.trim()).filter(Boolean))];
  return items.length ? items : undefined;
}

function toIntList(raw?: string): number[] | undefined {
  const list = toList(raw);
  if (!list) return undefined;
  const nums = list.map(Number).filter((n) => Number.isFinite(n));
  return nums.length ? nums : undefined;
}

/** Builds a JobEligibilityDto from parsed form values, omitting it entirely when no rule is set. */
function buildEligibility(v: JobFormParsed): JobEligibilityDto | undefined {
  const dto: JobEligibilityDto = {
    minCgpa: v.minCgpa,
    maxActiveBacklogs: v.maxActiveBacklogs,
    maxTotalBacklogs: v.maxTotalBacklogs,
    requiredWorkAuthorization: v.requiredWorkAuthorization || undefined,
    skillMatchMode: v.skillMatchMode || undefined,
    departments: toList(v.departments),
    branches: toList(v.branches),
    passingYears: toIntList(v.passingYears),
    requiredSkills: toList(v.requiredSkills),
    allowedLocations: toList(v.allowedLocations),
    allowedGenders: v.allowedGenders?.length ? v.allowedGenders : undefined,
    minPackage: v.minPackage,
    maxPackage: v.maxPackage,
  };
  const hasAnyRule = Object.values(dto).some((val) => val !== undefined);
  return hasAnyRule ? dto : undefined;
}

/** Formats an ISO instant as a value accepted by <input type="datetime-local"> (local time). */
function toDatetimeLocal(iso?: string): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const numOrEmpty = (n?: number): number | "" => (n == null ? "" : n);
const listOrEmpty = (arr?: (string | number)[]): string => (arr?.length ? arr.join(", ") : "");

/** Converts a JobResponse back into form values so the edit page can prefill every field. */
export function jobToFormValues(job: import("@/types/domain").JobResponse): JobFormValues {
  const e = job.eligibility;
  return {
    universityId: job.universityId,
    title: job.title,
    description: job.description,
    jobType: job.jobType,
    location: job.location ?? "",
    remoteAllowed: job.remoteAllowed,
    salaryMin: numOrEmpty(job.salaryMin),
    salaryMax: numOrEmpty(job.salaryMax),
    currency: job.currency ?? "",
    openings: numOrEmpty(job.openings),
    applicationDeadline: toDatetimeLocal(job.applicationDeadline),
    minCgpa: numOrEmpty(e?.minCgpa),
    maxActiveBacklogs: numOrEmpty(e?.maxActiveBacklogs),
    maxTotalBacklogs: numOrEmpty(e?.maxTotalBacklogs),
    requiredWorkAuthorization: e?.requiredWorkAuthorization ?? "",
    skillMatchMode: e?.skillMatchMode ?? "",
    departments: listOrEmpty(e?.departments),
    branches: listOrEmpty(e?.branches),
    passingYears: listOrEmpty(e?.passingYears),
    requiredSkills: listOrEmpty(e?.requiredSkills),
    allowedLocations: listOrEmpty(e?.allowedLocations),
    allowedGenders: e?.allowedGenders ?? [],
    minPackage: numOrEmpty(e?.minPackage),
    maxPackage: numOrEmpty(e?.maxPackage),
  };
}

/** Maps parsed form values onto the CreateJobRequest wire payload. */
export function toCreateJobRequest(v: JobFormParsed): CreateJobRequest {
  return {
    universityId: v.universityId,
    title: v.title,
    description: v.description,
    jobType: v.jobType,
    location: v.location || undefined,
    remoteAllowed: v.remoteAllowed,
    salaryMin: v.salaryMin,
    salaryMax: v.salaryMax,
    currency: v.currency || undefined,
    openings: v.openings,
    // datetime-local yields local wall-clock; convert to a UTC instant string for the backend.
    applicationDeadline: new Date(v.applicationDeadline).toISOString(),
    eligibility: buildEligibility(v),
  };
}
