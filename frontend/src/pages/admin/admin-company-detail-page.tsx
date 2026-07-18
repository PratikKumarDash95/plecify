import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import {
  useAdminCompany,
  useApproveCompany,
  useRejectCompany,
} from "@/features/admin/admin-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { Modal } from "@/components/ui/modal";
import { Textarea } from "@/components/ui/textarea";
import { CompanyStatusBadge } from "@/components/domain/status-badge";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState } from "@/components/ui/states";
import { formatDateTime } from "@/lib/format";
import { toApiError } from "@/lib/api-helpers";
import { paths } from "@/app/routes";

const rejectSchema = z.object({
  reason: z.string().min(1, "A reason is required").max(500),
});
type RejectFormValues = z.infer<typeof rejectSchema>;

export function AdminCompanyDetailPage() {
  const { companyId } = useParams<{ companyId: string }>();
  const navigate = useNavigate();
  const { data: company, isLoading, isError, error, refetch } = useAdminCompany(companyId);
  const approveCompany = useApproveCompany();
  const rejectCompany = useRejectCompany();
  const [showReject, setShowReject] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RejectFormValues>({
    resolver: zodResolver(rejectSchema),
    defaultValues: { reason: "" },
  });

  if (isLoading) return <LoadingState label="Loading company…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!company) return null;

  const isPending = company.status === "PENDING";

  const handleApprove = async () => {
    try {
      await approveCompany.mutateAsync(company.id);
      toast.success("Company approved. They can now post jobs.");
      navigate(paths.adminCompanies, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to approve company");
    }
  };

  const onReject = async (values: RejectFormValues) => {
    try {
      await rejectCompany.mutateAsync({ companyId: company.id, payload: { reason: values.reason } });
      toast.success("Company rejected");
      setShowReject(false);
      reset();
      navigate(paths.adminCompanies, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to reject company");
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={company.name}
        description={company.industry ?? "Company registration"}
        actions={
          isPending ? (
            <>
              <Button variant="danger" onClick={() => setShowReject(true)}>
                <Icon name="cancel" className="text-[18px]" /> Reject
              </Button>
              <Button isLoading={approveCompany.isPending} onClick={handleApprove}>
                <Icon name="check_circle" className="text-[18px]" /> Approve
              </Button>
            </>
          ) : undefined
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <CompanyStatusBadge status={company.status} />
        {company.website && (
          <a
            href={company.website}
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-1 text-body-md text-primary hover:underline"
          >
            <Icon name="link" className="text-[16px]" />
            {company.website}
          </a>
        )}
        {company.headquarters && (
          <span className="flex items-center gap-1 text-body-md text-on-surface-variant">
            <Icon name="location_on" className="text-[16px]" />
            {company.headquarters}
          </span>
        )}
      </div>

      {company.description && (
        <Card>
          <CardHeader>
            <CardTitle className="text-xl">About</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-body-md text-on-surface whitespace-pre-wrap">{company.description}</p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Contact & account</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-3">
          <Detail label="Contact person" value={company.contactPersonName} />
          <Detail label="Contact email" value={company.contactEmail} />
          <Detail label="Contact phone" value={company.contactPhone} />
          <Detail label="Account email" value={company.accountEmail} />
          <Detail label="Registered" value={formatDateTime(company.registeredAt)} />
        </CardContent>
      </Card>

      <Modal
        open={showReject}
        onClose={() => setShowReject(false)}
        title="Reject this company?"
        footer={
          <>
            <Button variant="outline" onClick={() => setShowReject(false)}>
              Cancel
            </Button>
            <Button
              variant="danger"
              type="submit"
              form="reject-company-form"
              isLoading={rejectCompany.isPending}
            >
              Reject company
            </Button>
          </>
        }
      >
        <form id="reject-company-form" onSubmit={handleSubmit(onReject)} noValidate>
          <p className="text-body-md text-on-surface-variant mb-3">
            The company will see this reason. They can re-register after addressing it.
          </p>
          <Textarea
            label="Reason"
            rows={4}
            placeholder="Explain why this registration was rejected…"
            error={errors.reason?.message}
            {...register("reason")}
          />
        </form>
      </Modal>
    </div>
  );
}

function Detail({ label, value }: { label: string; value?: string }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase tracking-wide text-on-surface-variant">{label}</dt>
      <dd className="text-body-md text-on-surface">{value ?? "—"}</dd>
    </div>
  );
}
