import { Button } from "./button";
import { Icon } from "./icon";

/**
 * Zero-based page navigator driven by the backend PagedResponse metadata.
 * `page` is the current zero-based index; `onPageChange` receives the new index.
 */
export function Pagination({
  page,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
}: {
  page: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}) {
  if (totalElements === 0) return null;

  const from = page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, totalElements);

  return (
    <div className="flex items-center justify-between gap-4 pt-4">
      <p className="text-sm text-on-surface-variant">
        Showing <span className="font-medium text-on-surface">{from}</span>–
        <span className="font-medium text-on-surface">{to}</span> of{" "}
        <span className="font-medium text-on-surface">{totalElements}</span>
      </p>
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
        >
          <Icon name="chevron_left" className="text-[18px]" />
          Previous
        </Button>
        <span className="text-sm text-on-surface-variant px-2">
          Page {page + 1} of {Math.max(totalPages, 1)}
        </span>
        <Button
          variant="outline"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Next
          <Icon name="chevron_right" className="text-[18px]" />
        </Button>
      </div>
    </div>
  );
}
