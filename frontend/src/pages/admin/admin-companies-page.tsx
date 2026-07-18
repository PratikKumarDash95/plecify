import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAdminCompanies } from "@/features/admin/admin-hooks";
import { PageHeader } from "@/components/ui/page-header";
import { Table, THead, TBody, TR, TH, TD } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { LoadingState } from "@/components/ui/spinner";
import { ErrorState, EmptyState } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { CompanyStatusBadge } from "@/components/domain/status-badge";
import { cn } from "@/lib/utils";
import { formatDate } from "@/lib/format";
import { paths } from "@/app/routes";
import type { ApprovalStatus } from "@/types/domain";

const PAGE_SIZE = 15;

const FILTERS: { value: ApprovalStatus; label: string }[] = [
  { value: "PENDING", label: "Pending" },
  { value: "APPROVED", label: "Approved" },
  { value: "REJECTED", label: "Rejected" },
];

const EMPTY_COPY: Record<ApprovalStatus, { title: string; description: string }> = {
  PENDING: {
    title: "Nothing to review",
    description: "New company registrations will appear here for approval.",
  },
  APPROVED: { title: "No approved companies", description: "Approved companies will be listed here." },
  REJECTED: { title: "No rejected companies", description: "Rejected companies will be listed here." },
};

export function AdminCompaniesPage() {
  const [status, setStatus] = useState<ApprovalStatus>("PENDING");
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useAdminCompanies(status, {
    page,
    size: PAGE_SIZE,
    sort: "registeredAt,asc",
  });

  const changeFilter = (next: ApprovalStatus) => {
    setStatus(next);
    setPage(0);
  };

  return (
    <div>
      <PageHeader
        title="Company registrations"
        description="Review and approve companies before they can post jobs."
      />

      <div className="mb-4 flex gap-2">
        {FILTERS.map((filter) => (
          <button
            key={filter.value}
            type="button"
            onClick={() => changeFilter(filter.value)}
            className={cn(
              "rounded-full px-4 py-1.5 text-sm font-medium transition-colors",
              status === filter.value
                ? "bg-primary-container text-white"
                : "bg-surface-container-high text-on-surface-variant hover:bg-surface-container-highest",
            )}
          >
            {filter.label}
          </button>
        ))}
      </div>

      {isLoading ? (
        <LoadingState label="Loading companies…" />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.content.length === 0 ? (
        <EmptyState icon="domain" title={EMPTY_COPY[status].title} description={EMPTY_COPY[status].description} />
      ) : (
        <>
          <Table>
            <THead>
              <TR>
                <TH>Company</TH>
                <TH>Industry</TH>
                <TH>Contact</TH>
                <TH>Status</TH>
                <TH>Registered</TH>
                <TH className="text-right">Action</TH>
              </TR>
            </THead>
            <TBody>
              {data.content.map((company) => (
                <TR
                  key={company.id}
                  className="cursor-pointer"
                  onClick={() => navigate(paths.adminCompanyDetail(company.id))}
                >
                  <TD className="font-medium text-primary">{company.name}</TD>
                  <TD className="text-on-surface-variant">{company.industry ?? "—"}</TD>
                  <TD className="text-on-surface-variant">
                    {company.contactPersonName ?? company.contactEmail ?? "—"}
                  </TD>
                  <TD>
                    <CompanyStatusBadge status={company.status} />
                  </TD>
                  <TD className="text-on-surface-variant whitespace-nowrap">
                    {formatDate(company.registeredAt)}
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
