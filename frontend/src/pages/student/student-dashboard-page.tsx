import { Link } from "react-router-dom";
import { useStudentDashboard } from "@/features/student/student-hooks";
import { useAuth } from "@/features/auth/use-auth";
import { StatCard } from "@/components/ui/stat-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Icon } from "@/components/ui/icon";
import { Badge } from "@/components/ui/badge";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { formatDeadline, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import { paths } from "@/app/routes";

export function StudentDashboardPage() {
  const { user } = useAuth();
  const { data, isLoading, isError, error, refetch } = useStudentDashboard();

  if (isLoading) return <LoadingState label="Loading your dashboard…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!data) return null;

  const firstName = user?.fullName.split(" ")[0] ?? "there";

  return (
    <div className="space-y-8">
      <section className="bg-white p-6 rounded-2xl shadow-ambient border border-outline-variant/20 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-primary-container/5 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none" />
        <div className="relative z-10 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-headline-md font-headline-md text-on-surface">
              Welcome back, {firstName} 👋
            </h1>
            <p className="text-body-md text-on-surface-variant">
              Here's what's happening with your placements.
            </p>
          </div>
          <div>
            {data.placementEligible ? (
              <Badge variant="success">
                <Icon name="verified" className="text-[16px]" /> Placement eligible
              </Badge>
            ) : (
              <Badge variant="warning">
                <Icon name="info" className="text-[16px]" /> Not placement eligible
              </Badge>
            )}
          </div>
        </div>
      </section>

      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Eligible jobs" value={data.eligibleJobs} icon="work" accent="primary" />
        <StatCard label="Applications" value={data.appliedJobs} icon="assignment" accent="tertiary" />
        <StatCard label="Interviews" value={data.interviewCount} icon="event_available" accent="success" />
      </section>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-xl">Upcoming deadlines</CardTitle>
          <Link
            to={paths.studentJobs}
            className="text-sm font-medium text-primary hover:text-on-primary-fixed-variant"
          >
            Browse all jobs
          </Link>
        </CardHeader>
        <CardContent>
          {data.upcomingDeadlines.length === 0 ? (
            <EmptyState
              icon="event_busy"
              title="No upcoming deadlines"
              description="Jobs you're eligible for will show their deadlines here."
            />
          ) : (
            <ul className="divide-y divide-outline-variant/40">
              {data.upcomingDeadlines.map((job) => {
                const deadline = formatDeadline(job.applicationDeadline);
                return (
                  <li key={job.eligibleJobId} className="py-3 flex items-center justify-between gap-4">
                    <div className="min-w-0">
                      <p className="font-medium text-on-surface truncate">{job.title}</p>
                      <p className="text-sm text-on-surface-variant truncate">
                        {job.companyName} · {jobTypeLabels[job.jobType]} ·{" "}
                        {formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
                      </p>
                    </div>
                    <Badge variant={deadline.urgent ? "danger" : "neutral"}>{deadline.text}</Badge>
                  </li>
                );
              })}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
