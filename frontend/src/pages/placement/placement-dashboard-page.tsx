import { Link } from "react-router-dom";
import { usePlacementDashboard } from "@/features/placement/placement-hooks";
import { StatCard } from "@/components/ui/stat-card";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState } from "@/components/ui/states";
import { paths } from "@/app/routes";

export function PlacementDashboardPage() {
  const { data, isLoading, isError, error, refetch } = usePlacementDashboard();

  if (isLoading) return <LoadingState label="Loading dashboard…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!data) return null;

  return (
    <div className="space-y-8">
      <PageHeader
        title="Placement cell overview"
        description="Review job postings and monitor placement activity."
        actions={
          <Link to={paths.placementPending}>
            <Button>
              <Icon name="fact_check" className="text-[18px]" /> Review queue
              {data.pendingJobs > 0 && (
                <span className="ml-1 rounded-full bg-white/25 px-1.5 text-xs">{data.pendingJobs}</span>
              )}
            </Button>
          </Link>
        }
      />

      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Pending review" value={data.pendingJobs} icon="pending_actions" accent="warning" />
        <StatCard label="Approved today" value={data.approvedToday} icon="check_circle" accent="success" />
        <StatCard label="Rejected today" value={data.rejectedToday} icon="cancel" accent="danger" />
        <StatCard label="Total applications" value={data.totalApplications} icon="assignment" accent="tertiary" />
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Job review totals</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Row label="Approved (all time)" value={data.approvedJobs} icon="check_circle" />
            <Row label="Rejected (all time)" value={data.rejectedJobs} icon="cancel" />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Students</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Row label="Total students" value={data.totalStudents} icon="school" />
            <Row label="Placement eligible" value={data.placementEligibleStudents} icon="verified" />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Row({ label, value, icon }: { label: string; value: number; icon: string }) {
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
