import { useState } from "react";
import { useEligibleJobs } from "@/features/student/student-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { ApplyModal } from "./apply-modal";
import { formatDeadline, formatSalaryRange, jobTypeLabels } from "@/lib/format";
import type { EligibleJobResponse } from "@/types/domain";

const PAGE_SIZE = 12;

export function EligibleJobsPage() {
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<EligibleJobResponse | null>(null);
  const { data, isLoading, isError, error, refetch } = useEligibleJobs({
    page,
    size: PAGE_SIZE,
    sort: "matchedAt,desc",
  });

  return (
    <div>
      <PageHeader
        title="Eligible jobs"
        description="Roles you qualify for based on your academic profile."
      />

      {isLoading ? (
        <LoadingState label="Finding jobs for you…" />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon="work_off"
          title="No eligible jobs yet"
          description="When companies post roles matching your profile, they'll appear here."
        />
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {data.content.map((job) => {
              const deadline = formatDeadline(job.applicationDeadline);
              const applied = job.eligibilityStatus === "APPLIED";
              return (
                <Card key={job.eligibleJobId} className="flex flex-col">
                  <CardContent className="pt-6 flex flex-col gap-3 flex-1">
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0">
                        <h3 className="font-semibold text-on-surface truncate">{job.title}</h3>
                        <p className="text-sm text-on-surface-variant truncate">{job.companyName}</p>
                      </div>
                      <Badge variant="info">{jobTypeLabels[job.jobType]}</Badge>
                    </div>

                    <div className="space-y-1.5 text-sm text-on-surface-variant">
                      {job.location && (
                        <p className="flex items-center gap-1.5">
                          <Icon name="location_on" className="text-[16px]" />
                          {job.location}
                          {job.remoteAllowed && " · Remote OK"}
                        </p>
                      )}
                      <p className="flex items-center gap-1.5">
                        <Icon name="payments" className="text-[16px]" />
                        {formatSalaryRange(job.salaryMin, job.salaryMax, job.currency)}
                      </p>
                      <p className="flex items-center gap-1.5">
                        <Icon name="event" className="text-[16px]" />
                        <span className={deadline.urgent ? "text-error font-medium" : ""}>
                          {deadline.text}
                        </span>
                      </p>
                    </div>

                    <div className="mt-auto pt-2">
                      {applied ? (
                        <Button variant="outline" className="w-full" disabled>
                          <Icon name="check" className="text-[18px]" /> Applied
                        </Button>
                      ) : (
                        <Button className="w-full" onClick={() => setSelected(job)}>
                          Apply now
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            pageSize={data.size}
            onPageChange={setPage}
          />
        </>
      )}

      <ApplyModal job={selected} open={!!selected} onClose={() => setSelected(null)} />
    </div>
  );
}
