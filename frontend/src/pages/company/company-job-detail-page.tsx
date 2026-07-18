import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { useCompanyJob, useDeleteJob } from "@/features/jobs/job-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { Modal } from "@/components/ui/modal";
import { StatCard } from "@/components/ui/stat-card";
import { JobStatusBadge } from "@/components/domain/status-badge";
import { EligibilitySummary } from "@/components/domain/eligibility-summary";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState } from "@/components/ui/states";
import { formatDateTime, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import { toApiError } from "@/lib/api-helpers";
import { paths } from "@/app/routes";

export function CompanyJobDetailPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { data: job, isLoading, isError, error, refetch } = useCompanyJob(jobId);
  const deleteJob = useDeleteJob();
  const [confirmDelete, setConfirmDelete] = useState(false);

  if (isLoading) return <LoadingState label="Loading job…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!job) return null;

  const isPending = job.status === "PENDING";

  const handleDelete = async () => {
    try {
      await deleteJob.mutateAsync(job.id);
      toast.success("Job deleted");
      navigate(paths.companyJobs, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to delete job");
    } finally {
      setConfirmDelete(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={job.title}
        actions={
          isPending ? (
            <>
              <Link to={paths.companyJobEdit(job.id)}>
                <Button variant="outline">
                  <Icon name="edit" className="text-[18px]" /> Edit
                </Button>
              </Link>
              <Button variant="danger" onClick={() => setConfirmDelete(true)}>
                <Icon name="delete" className="text-[18px]" /> Delete
              </Button>
            </>
          ) : undefined
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <JobStatusBadge status={job.status} />
        <span className="text-body-md text-on-surface-variant">{jobTypeLabels[job.jobType]}</span>
        {job.location && (
          <span className="flex items-center gap-1 text-body-md text-on-surface-variant">
            <Icon name="location_on" className="text-[16px]" />
            {job.location}
            {job.remoteAllowed && " · Remote OK"}
          </span>
        )}
      </div>

      {job.status === "REJECTED" && job.rejectionReason && (
        <div className="rounded-xl border border-error/30 bg-error-container/40 p-4">
          <p className="font-medium text-on-error-container flex items-center gap-2">
            <Icon name="cancel" className="text-[18px]" /> Rejected
          </p>
          <p className="text-body-md text-on-surface mt-1">{job.rejectionReason}</p>
        </div>
      )}

      <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Eligible students" value={job.eligibleStudentCount} icon="groups" accent="primary" />
        <StatCard label="Applications" value={job.applicationCount} icon="assignment_ind" accent="tertiary" />
        <StatCard
          label="Openings"
          value={job.openings}
          icon="event_seat"
          accent="success"
          hint={formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
        />
      </section>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Description</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-body-md text-on-surface whitespace-pre-wrap">{job.description}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Eligibility</CardTitle>
        </CardHeader>
        <CardContent>
          <EligibilitySummary eligibility={job.eligibility} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Timeline</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-3">
          <Detail label="Application deadline" value={formatDateTime(job.applicationDeadline)} />
          <Detail label="Created" value={formatDateTime(job.createdAt)} />
          {job.reviewedAt && <Detail label="Reviewed" value={formatDateTime(job.reviewedAt)} />}
          {job.approvedAt && <Detail label="Approved" value={formatDateTime(job.approvedAt)} />}
        </CardContent>
      </Card>

      <Modal
        open={confirmDelete}
        onClose={() => setConfirmDelete(false)}
        title="Delete this job?"
        size="sm"
        footer={
          <>
            <Button variant="outline" onClick={() => setConfirmDelete(false)}>
              Cancel
            </Button>
            <Button variant="danger" isLoading={deleteJob.isPending} onClick={handleDelete}>
              Delete
            </Button>
          </>
        }
      >
        <p className="text-body-md text-on-surface-variant">
          This permanently deletes "{job.title}". This action can't be undone.
        </p>
      </Modal>
    </div>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase tracking-wide text-on-surface-variant">{label}</dt>
      <dd className="text-body-md text-on-surface">{value}</dd>
    </div>
  );
}
