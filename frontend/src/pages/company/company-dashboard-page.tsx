import { Link } from "react-router-dom";
import { useCompanyDashboard } from "@/features/jobs/job-hooks";
import { StatCard } from "@/components/ui/stat-card";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { applicationStatusLabels } from "@/lib/format";
import { paths } from "@/app/routes";
import type { ApplicationStatus } from "@/types/domain";

export function CompanyDashboardPage() {
  const { data, isLoading, isError, error, refetch } = useCompanyDashboard();

  if (isLoading) return <LoadingState label="Loading dashboard…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!data) return null;

  const statusEntries = Object.entries(data.applicationsByStatus).filter(([, n]) => n > 0);

  return (
    <div className="space-y-8">
      <PageHeader
        title="Company overview"
        description="Your postings and applicant pipeline at a glance."
        actions={
          <Link to={paths.companyJobNew}>
            <Button>
              <Icon name="add" className="text-[18px]" /> Post a job
            </Button>
          </Link>
        }
      />

      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total jobs" value={data.totalJobs} icon="work" accent="primary" />
        <StatCard label="Pending review" value={data.pendingJobs} icon="pending_actions" accent="warning" />
        <StatCard label="Approved" value={data.approvedJobs} icon="check_circle" accent="success" />
        <StatCard label="Applications" value={data.totalApplications} icon="group" accent="tertiary" />
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Jobs by status</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <StatusRow label="Pending" value={data.pendingJobs} icon="pending_actions" />
            <StatusRow label="Approved" value={data.approvedJobs} icon="check_circle" />
            <StatusRow label="Rejected" value={data.rejectedJobs} icon="cancel" />
            <StatusRow label="Closed" value={data.closedJobs} icon="lock" />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Applications by stage</CardTitle>
          </CardHeader>
          <CardContent>
            {statusEntries.length === 0 ? (
              <EmptyState icon="inbox" title="No applications yet" />
            ) : (
              <div className="space-y-3">
                {statusEntries.map(([status, count]) => (
                  <StatusRow
                    key={status}
                    label={applicationStatusLabels[status as ApplicationStatus] ?? status}
                    value={count}
                    icon="person"
                  />
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function StatusRow({ label, value, icon }: { label: string; value: number; icon: string }) {
  return (
    <div className="flex items-center justify-between">
      <span className="flex items-center gap-2 text-on-surface-variant">
        <Icon name={icon} className="text-[18px]" />
        {label}
      </span>
      <span className="font-semibold text-on-surface">{value}</span>
    </div>
  );
}
