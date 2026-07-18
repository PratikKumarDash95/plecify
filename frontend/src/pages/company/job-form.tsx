import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import { AiTextField } from "@/features/ai/ai-text-field";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { jobFormSchema, type JobFormValues, type JobFormParsed } from "@/features/jobs/job-schema";
import { jobTypeLabels, workAuthorizationLabels } from "@/lib/format";
import type { JobType, WorkAuthorization } from "@/types/domain";

const jobTypeOptions = (Object.keys(jobTypeLabels) as JobType[]).map((v) => ({
  value: v,
  label: jobTypeLabels[v],
}));

const workAuthOptions = [
  { value: "", label: "Any (no requirement)" },
  ...(Object.keys(workAuthorizationLabels) as WorkAuthorization[]).map((v) => ({
    value: v,
    label: workAuthorizationLabels[v],
  })),
];

const skillModeOptions = [
  { value: "", label: "Not specified" },
  { value: "ALL", label: "Must match all skills" },
  { value: "ANY", label: "Match any skill" },
];

const genderOptions: { value: "MALE" | "FEMALE" | "OTHER" | "UNDISCLOSED"; label: string }[] = [
  { value: "MALE", label: "Male" },
  { value: "FEMALE", label: "Female" },
  { value: "OTHER", label: "Other" },
  { value: "UNDISCLOSED", label: "Undisclosed" },
];

export function JobForm({
  defaultValues,
  submitLabel,
  isSubmitting,
  onSubmit,
  onCancel,
}: {
  defaultValues: JobFormValues;
  submitLabel: string;
  isSubmitting: boolean;
  onSubmit: (payload: JobFormParsed) => void;
  onCancel: () => void;
}) {
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<JobFormValues, unknown, JobFormParsed>({
    resolver: zodResolver(jobFormSchema),
    defaultValues,
  });

  return (
    <form className="space-y-6" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Role details</CardTitle>
          <CardDescription>Core information students will see once approved.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Input label="Job title" error={errors.title?.message} {...register("title")} />
          <Controller
            control={control}
            name="description"
            render={({ field }) => (
              <AiTextField
                label="Description"
                rows={6}
                aiContext="job description"
                error={errors.description?.message}
                value={field.value}
                onChange={field.onChange}
                onBlur={field.onBlur}
                name={field.name}
                onAiResult={(text) => field.onChange(text)}
              />
            )}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select
              label="Job type"
              options={jobTypeOptions}
              error={errors.jobType?.message}
              {...register("jobType")}
            />
            <Input label="Location" leadingIcon="location_on" error={errors.location?.message} {...register("location")} />
          </div>
          <label className="flex items-center gap-2 text-body-md text-on-surface">
            <input type="checkbox" className="h-4 w-4 rounded border-outline-variant" {...register("remoteAllowed")} />
            Remote work allowed
          </label>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Input label="Salary min" type="number" error={errors.salaryMin?.message} {...register("salaryMin")} />
            <Input label="Salary max" type="number" error={errors.salaryMax?.message} {...register("salaryMax")} />
            <Input label="Currency" placeholder="INR" maxLength={3} error={errors.currency?.message} {...register("currency")} />
            <Input label="Openings" type="number" error={errors.openings?.message} {...register("openings")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="University ID"
              hint="UUID of the target campus."
              error={errors.universityId?.message}
              {...register("universityId")}
            />
            <Input
              label="Application deadline"
              type="datetime-local"
              error={errors.applicationDeadline?.message}
              {...register("applicationDeadline")}
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Eligibility rules</CardTitle>
          <CardDescription>
            Optional. Leave blank for an open-to-all posting. Lists accept comma-separated values.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="Min CGPA" type="number" step="0.01" error={errors.minCgpa?.message} {...register("minCgpa")} />
            <Input label="Max active backlogs" type="number" error={errors.maxActiveBacklogs?.message} {...register("maxActiveBacklogs")} />
            <Input label="Max total backlogs" type="number" error={errors.maxTotalBacklogs?.message} {...register("maxTotalBacklogs")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Departments" placeholder="CSE, ECE" error={errors.departments?.message} {...register("departments")} />
            <Input label="Branches" placeholder="Computer Science, IT" error={errors.branches?.message} {...register("branches")} />
            <Input label="Passing years" placeholder="2025, 2026" error={errors.passingYears?.message} {...register("passingYears")} />
            <Input label="Allowed locations" placeholder="Bengaluru, Pune" error={errors.allowedLocations?.message} {...register("allowedLocations")} />
          </div>
          <Input label="Required skills" placeholder="Java, React, SQL" error={errors.requiredSkills?.message} {...register("requiredSkills")} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select label="Skill match mode" options={skillModeOptions} error={errors.skillMatchMode?.message} {...register("skillMatchMode")} />
            <Select label="Work authorization" options={workAuthOptions} error={errors.requiredWorkAuthorization?.message} {...register("requiredWorkAuthorization")} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Min package" type="number" error={errors.minPackage?.message} {...register("minPackage")} />
            <Input label="Max package" type="number" error={errors.maxPackage?.message} {...register("maxPackage")} />
          </div>
          <div>
            <span className="block text-label-md font-label-md text-on-surface mb-1.5">
              Allowed genders
            </span>
            <Controller
              control={control}
              name="allowedGenders"
              render={({ field }) => (
                <div className="flex flex-wrap gap-4">
                  {genderOptions.map((opt) => {
                    const current = field.value ?? [];
                    const checked = current.includes(opt.value);
                    return (
                      <label key={opt.value} className="flex items-center gap-2 text-body-md text-on-surface">
                        <input
                          type="checkbox"
                          className="h-4 w-4 rounded border-outline-variant"
                          checked={checked}
                          onChange={(e) => {
                            field.onChange(
                              e.target.checked
                                ? [...current, opt.value]
                                : current.filter((v) => v !== opt.value),
                            );
                          }}
                        />
                        {opt.label}
                      </label>
                    );
                  })}
                </div>
              )}
            />
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end gap-3">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" isLoading={isSubmitting}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
