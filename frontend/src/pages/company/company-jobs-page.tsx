import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCompanyJobs } from "@/features/jobs/job-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Table, THead, TBody, TR, TH, TD } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { JobStatusBadge } from "@/components/domain/status-badge";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { formatDate, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import { paths } from "@/app/routes";

const PAGE_SIZE = 15;

export function CompanyJobsPage() {
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useCompanyJobs({
    page,
    size: PAGE_SIZE,
    sort: "createdAt,desc",
  });

  return (
    <div>
      <PageHeader
        title="My jobs"
        description="Every posting you've created, across all statuses."
        actions={
          <Link to={paths.companyJobNew}>
            <Button>
              <Icon name="add" className="text-[18px]" /> Post a job
            </Button>
          </Link>
        }
      />

      {isLoading ? (
        <LoadingState label="Loading your jobs…" />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon="work_off"
          title="No jobs yet"
          description="Post your first role to start receiving applications."
          action={
            <Link to={paths.companyJobNew}>
              <Button>Post a job</Button>
            </Link>
          }
        />
      ) : (
        <>
          <Table>
            <THead>
              <TR>
                <TH>Title</TH>
                <TH>Type</TH>
                <TH>Salary</TH>
                <TH>Openings</TH>
                <TH>Status</TH>
                <TH>Created</TH>
              </TR>
            </THead>
            <TBody>
              {data.content.map((job) => (
                <TR
                  key={job.id}
                  className="cursor-pointer"
                  onClick={() => navigate(paths.companyJobDetail(job.id))}
                >
                  <TD className="font-medium text-primary">{job.title}</TD>
                  <TD className="text-on-surface-variant">{jobTypeLabels[job.jobType]}</TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
                  </TD>
                  <TD className="text-on-surface-variant">{job.openings}</TD>
                  <TD>
                    <JobStatusBadge status={job.status} />
                  </TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatDate(job.createdAt)}
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
