import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { PageHeader } from "@/components/ui/page-header";
import { JobForm } from "./job-form";
import { useCompanyJob, useUpdateJob } from "@/features/jobs/job-hooks";
import { jobToFormValues, toCreateJobRequest, type JobFormParsed } from "@/features/jobs/job-schema";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { toApiError } from "@/lib/api-helpers";
import { paths } from "@/app/routes";

export function CompanyJobEditPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { data: job, isLoading, isError, error, refetch } = useCompanyJob(jobId);
  const updateJob = useUpdateJob(jobId ?? "");

  if (isLoading) return <LoadingState label="Loading job…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!job) return null;

  // Only PENDING jobs are editable server-side; guide the user back rather than let them fail.
  if (job.status !== "PENDING") {
    return (
      <EmptyState
        icon="lock"
        title="This job can't be edited"
        description="Only jobs still pending review can be changed."
      />
    );
  }

  const handleSubmit = async (values: JobFormParsed) => {
    try {
      await updateJob.mutateAsync(toCreateJobRequest(values));
      toast.success("Job updated");
      navigate(paths.companyJobDetail(job.id), { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to update job");
    }
  };

  return (
    <div>
      <PageHeader title="Edit job" description="Changes keep the job in review." />
      <JobForm
        defaultValues={jobToFormValues(job)}
        submitLabel="Save changes"
        isSubmitting={updateJob.isPending}
        onSubmit={handleSubmit}
        onCancel={() => navigate(paths.companyJobDetail(job.id))}
      />
    </div>
  );
}
