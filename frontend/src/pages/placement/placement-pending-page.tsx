import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { usePendingJobs } from "@/features/placement/placement-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Table, THead, TBody, TR, TH, TD } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { formatDate, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import { paths } from "@/app/routes";

const PAGE_SIZE = 15;

export function PlacementPendingPage() {
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = usePendingJobs({
    page,
    size: PAGE_SIZE,
    sort: "createdAt,asc",
  });

  return (
    <div>
      <PageHeader
        title="Review queue"
        description="Jobs awaiting approval before students can see them."
      />

      {isLoading ? (
        <LoadingState label="Loading pending jobs…" />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon="task_alt"
          title="Nothing to review"
          description="You're all caught up. New submissions will appear here."
        />
      ) : (
        <>
          <Table>
            <THead>
              <TR>
                <TH>Title</TH>
                <TH>Company</TH>
                <TH>Type</TH>
                <TH>Salary</TH>
                <TH>Submitted</TH>
                <TH className="text-right">Action</TH>
              </TR>
            </THead>
            <TBody>
              {data.content.map((job) => (
                <TR
                  key={job.id}
                  className="cursor-pointer"
                  onClick={() => navigate(paths.placementJobDetail(job.id))}
                >
                  <TD className="font-medium text-primary">{job.title}</TD>
                  <TD className="text-on-surface-variant">{job.companyName}</TD>
                  <TD className="text-on-surface-variant">{jobTypeLabels[job.jobType]}</TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
                  </TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatDate(job.createdAt)}
                  </TD>
                  <TD className="text-right">
                    <Button variant="outline" size="sm">
                      Review
                      <Icon name="chevron_right" className="text-[18px]" />
                    </Button>
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
