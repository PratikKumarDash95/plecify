import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { usePlacementJob, useApproveJob, useRejectJob } from "@/features/placement/placement-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { Modal } from "@/components/ui/modal";
import { AiTextField } from "@/features/ai/ai-text-field";
import { StatCard } from "@/components/ui/stat-card";
import { JobStatusBadge } from "@/components/domain/status-badge";
import { EligibilitySummary } from "@/components/domain/eligibility-summary";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState } from "@/components/ui/states";
import { formatDateTime, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import { toApiError } from "@/lib/api-helpers";
import { paths } from "@/app/routes";

const rejectSchema = z.object({
  reason: z.string().min(1, "A reason is required").max(500),
});
type RejectFormValues = z.infer<typeof rejectSchema>;

export function PlacementJobDetailPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const { data: job, isLoading, isError, error, refetch } = usePlacementJob(jobId);
  const approveJob = useApproveJob();
  const rejectJob = useRejectJob();
  const [showReject, setShowReject] = useState(false);

  const {
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm<RejectFormValues>({
    resolver: zodResolver(rejectSchema),
    defaultValues: { reason: "" },
  });

  if (isLoading) return <LoadingState label="Loading job…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!job) return null;

  const isPending = job.status === "PENDING";

  const handleApprove = async () => {
    try {
      const result = await approveJob.mutateAsync(job.id);
      toast.success(
        `Approved. Matched ${result.eligibleStudentsMatched} students, sent ${result.notificationsDispatched} notifications.`,
      );
      navigate(paths.placementPending, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to approve job");
    }
  };

  const onReject = async (values: RejectFormValues) => {
    try {
      await rejectJob.mutateAsync({ jobId: job.id, payload: { reason: values.reason } });
      toast.success("Job rejected");
      setShowReject(false);
      reset();
      navigate(paths.placementPending, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to reject job");
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={job.title}
        description={job.companyName}
        actions={
          isPending ? (
            <>
              <Button variant="danger" onClick={() => setShowReject(true)}>
                <Icon name="cancel" className="text-[18px]" /> Reject
              </Button>
              <Button isLoading={approveJob.isPending} onClick={handleApprove}>
                <Icon name="check_circle" className="text-[18px]" /> Approve
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

      <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Openings" value={job.openings} icon="event_seat" accent="primary" />
        <StatCard
          label="Salary"
          value={formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
          icon="payments"
          accent="tertiary"
        />
        <StatCard label="Eligible (est.)" value={job.eligibleStudentCount} icon="groups" accent="success" />
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
          <CardTitle className="text-xl">Details</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-3">
          <Detail label="Application deadline" value={formatDateTime(job.applicationDeadline)} />
          <Detail label="Submitted" value={formatDateTime(job.createdAt)} />
        </CardContent>
      </Card>

      <Modal
        open={showReject}
        onClose={() => setShowReject(false)}
        title="Reject this job?"
        footer={
          <>
            <Button variant="outline" onClick={() => setShowReject(false)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              type="submit"
              form="reject-form"
              isLoading={rejectJob.isPending}
            >
              Reject job
            </Button>
          </>
        }
      >
        <form id="reject-form" onSubmit={handleSubmit(onReject)} noValidate>
          <p className="text-body-md text-on-surface-variant mb-3">
            The company will see this reason. Rejected jobs can't be resubmitted.
          </p>
          <Controller
            control={control}
            name="reason"
            render={({ field }) => (
              <AiTextField
                label="Reason"
                rows={4}
                placeholder="Explain why this posting was rejected…"
                aiContext="job rejection reason"
                error={errors.reason?.message}
                value={field.value}
                onChange={field.onChange}
                onBlur={field.onBlur}
                name={field.name}
                onAiResult={(text) => field.onChange(text)}
              />
            )}
          />
        </form>
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
