import { Icon } from "@/components/ui/icon";
import { workAuthorizationLabels } from "@/lib/format";
import type { JobEligibilityDto } from "@/types/domain";

const genderLabels: Record<string, string> = {
  MALE: "Male",
  FEMALE: "Female",
  OTHER: "Other",
  UNDISCLOSED: "Undisclosed",
};

/** Renders a job's eligibility rules as a readable list, or an "open to all" note when absent. */
export function EligibilitySummary({ eligibility }: { eligibility?: JobEligibilityDto }) {
  if (!eligibility) {
    return (
      <p className="flex items-center gap-2 text-body-md text-on-surface-variant">
        <Icon name="public" className="text-[18px]" /> Open to all eligible students.
      </p>
    );
  }

  const rows: { label: string; value: string }[] = [];
  const e = eligibility;
  if (e.minCgpa != null) rows.push({ label: "Minimum CGPA", value: String(e.minCgpa) });
  if (e.maxActiveBacklogs != null) rows.push({ label: "Max active backlogs", value: String(e.maxActiveBacklogs) });
  if (e.maxTotalBacklogs != null) rows.push({ label: "Max total backlogs", value: String(e.maxTotalBacklogs) });
  if (e.departments?.length) rows.push({ label: "Departments", value: e.departments.join(", ") });
  if (e.branches?.length) rows.push({ label: "Branches", value: e.branches.join(", ") });
  if (e.passingYears?.length) rows.push({ label: "Passing years", value: e.passingYears.join(", ") });
  if (e.requiredSkills?.length) {
    const mode = e.skillMatchMode === "ANY" ? " (any)" : e.skillMatchMode === "ALL" ? " (all)" : "";
    rows.push({ label: `Required skills${mode}`, value: e.requiredSkills.join(", ") });
  }
  if (e.allowedLocations?.length) rows.push({ label: "Locations", value: e.allowedLocations.join(", ") });
  if (e.allowedGenders?.length)
    rows.push({ label: "Genders", value: e.allowedGenders.map((g) => genderLabels[g] ?? g).join(", ") });
  if (e.requiredWorkAuthorization)
    rows.push({ label: "Work authorization", value: workAuthorizationLabels[e.requiredWorkAuthorization] });
  if (e.minPackage != null) rows.push({ label: "Min package", value: String(e.minPackage) });
  if (e.maxPackage != null) rows.push({ label: "Max package", value: String(e.maxPackage) });

  if (rows.length === 0) {
    return (
      <p className="flex items-center gap-2 text-body-md text-on-surface-variant">
        <Icon name="public" className="text-[18px]" /> Open to all eligible students.
      </p>
    );
  }

  return (
    <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-3">
      {rows.map((row) => (
        <div key={row.label} className="flex flex-col">
          <dt className="text-xs uppercase tracking-wide text-on-surface-variant">{row.label}</dt>
          <dd className="text-body-md text-on-surface">{row.value}</dd>
        </div>
      ))}
    </dl>
  );
}
