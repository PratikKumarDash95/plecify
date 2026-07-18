import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { PageHeader } from "@/components/ui/page-header";
import { JobForm } from "./job-form";
import { useCreateJob } from "@/features/jobs/job-hooks";
import { toCreateJobRequest, type JobFormValues, type JobFormParsed } from "@/features/jobs/job-schema";
import { toApiError } from "@/lib/api-helpers";
import { paths } from "@/app/routes";

const emptyDefaults: JobFormValues = {
  universityId: "",
  title: "",
  description: "",
  jobType: "FULL_TIME",
  location: "",
  remoteAllowed: false,
  salaryMin: "",
  salaryMax: "",
  currency: "INR",
  openings: 1,
  applicationDeadline: "",
  minCgpa: "",
  maxActiveBacklogs: "",
  maxTotalBacklogs: "",
  requiredWorkAuthorization: "",
  skillMatchMode: "",
  departments: "",
  branches: "",
  passingYears: "",
  requiredSkills: "",
  allowedLocations: "",
  allowedGenders: [],
  minPackage: "",
  maxPackage: "",
};

export function CompanyJobNewPage() {
  const navigate = useNavigate();
  const createJob = useCreateJob();

  const handleSubmit = async (values: JobFormParsed) => {
    try {
      const job = await createJob.mutateAsync(toCreateJobRequest(values));
      toast.success("Job submitted for review");
      navigate(paths.companyJobDetail(job.id), { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to create job");
    }
  };

  return (
    <div>
      <PageHeader title="Post a job" description="New postings enter review before students can see them." />
      <JobForm
        defaultValues={emptyDefaults}
        submitLabel="Submit for review"
        isSubmitting={createJob.isPending}
        onSubmit={handleSubmit}
        onCancel={() => navigate(paths.companyJobs)}
      />
    </div>
  );
}
