import { Link } from "react-router-dom";
import { useAdminCompanies } from "@/features/admin/admin-hooks";
import { StatCard } from "@/components/ui/stat-card";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState } from "@/components/ui/states";
import { CompanyStatusBadge } from "@/components/domain/status-badge";
import { formatDate } from "@/lib/format";
import { paths } from "@/app/routes";

/**
 * Admin overview. The backend exposes no dedicated admin-dashboard endpoint, so the counts here
 * are derived from the paged company lists (totalElements per status) rather than a summary call.
 */
export function AdminDashboardPage() {
  const pending = useAdminCompanies("PENDING", { page: 0, size: 5, sort: "registeredAt,asc" });
  const approved = useAdminCompanies("APPROVED", { page: 0, size: 1 });
  const rejected = useAdminCompanies("REJECTED", { page: 0, size: 1 });

  if (pending.isLoading) return <LoadingState label="Loading dashboard…" />;
  if (pending.isError) return <ErrorState error={pending.error} onRetry={pending.refetch} />;

  const pendingCount = pending.data?.totalElements ?? 0;

  return (
    <div className="space-y-8">
      <PageHeader
        title="Admin overview"
        description="Approve company registrations and keep the marketplace trusted."
        actions={
          <Link to={paths.adminCompanies}>
            <Button>
              <Icon name="domain" className="text-[18px]" /> Review companies
              {pendingCount > 0 && (
                <span className="ml-1 rounded-full bg-white/25 px-1.5 text-xs">{pendingCount}</span>
              )}
            </Button>
          </Link>
        }
      />

      <section className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Pending review" value={pendingCount} icon="pending_actions" accent="warning" />
        <StatCard
          label="Approved"
          value={approved.data?.totalElements ?? 0}
          icon="check_circle"
          accent="success"
        />
        <StatCard
          label="Rejected"
          value={rejected.data?.totalElements ?? 0}
          icon="cancel"
          accent="danger"
        />
      </section>

      <Card>
        <CardHeader>
          <CardTitle className="text-xl">Awaiting review</CardTitle>
        </CardHeader>
        <CardContent>
          {!pending.data || pending.data.content.length === 0 ? (
            <p className="text-body-md text-on-surface-variant">You're all caught up.</p>
          ) : (
            <ul className="divide-y divide-surface-variant">
              {pending.data.content.map((company) => (
                <li key={company.id}>
                  <Link
                    to={paths.adminCompanyDetail(company.id)}
                    className="flex items-center justify-between gap-3 py-3 hover:opacity-80"
                  >
                    <div className="min-w-0">
                      <p className="truncate font-medium text-on-surface">{company.name}</p>
                      <p className="truncate text-sm text-on-surface-variant">
                        {company.industry ?? "—"} · {formatDate(company.registeredAt)}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <CompanyStatusBadge status={company.status} />
                      <Icon name="chevron_right" className="text-on-surface-variant" />
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
