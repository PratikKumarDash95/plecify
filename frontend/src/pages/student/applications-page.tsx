import { useState } from "react";
import { Link } from "react-router-dom";
import { useStudentApplications } from "@/features/student/student-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Table, THead, TBody, TR, TH, TD } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { ApplicationStatusBadge } from "@/components/domain/status-badge";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { formatDate, formatDateTime } from "@/lib/format";
import { paths } from "@/app/routes";

const PAGE_SIZE = 15;

export function ApplicationsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, error, refetch } = useStudentApplications({
    page,
    size: PAGE_SIZE,
    sort: "createdAt,desc",
  });

  return (
    <div>
      <PageHeader title="My applications" description="Track the status of every role you've applied to." />

      {isLoading ? (
        <LoadingState label="Loading your applications…" />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon="assignment"
          title="No applications yet"
          description="Once you apply to a job, it'll show up here."
          action={
            <Link to={paths.studentJobs}>
              <Button>Browse eligible jobs</Button>
            </Link>
          }
        />
      ) : (
        <>
          <Table>
            <THead>
              <TR>
                <TH>Job</TH>
                <TH>Company</TH>
                <TH>Status</TH>
                <TH>Interview</TH>
                <TH>Applied</TH>
              </TR>
            </THead>
            <TBody>
              {data.content.map((app) => (
                <TR key={app.id}>
                  <TD className="font-medium">{app.jobTitle}</TD>
                  <TD className="text-on-surface-variant">{app.companyName}</TD>
                  <TD>
                    <ApplicationStatusBadge status={app.status} />
                  </TD>
                  <TD className="text-on-surface-variant">
                    {app.interviewAt ? formatDateTime(app.interviewAt) : "—"}
                  </TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatDate(app.createdAt)}
                  </TD>
                </TR>
              ))}
            </TBody>
          </Table>

          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            pageSize={data.size}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
