import type { ReactNode } from "react";
import { Icon } from "./icon";
import { Button } from "./button";
import { toApiError } from "@/lib/api-helpers";

/** Neutral empty state for lists/tables with no rows. */
export function EmptyState({
  icon = "inbox",
  title,
  description,
  action,
}: {
  icon?: string;
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-surface-container-high text-on-surface-variant">
        <Icon name={icon} className="text-3xl" />
      </div>
      <div className="space-y-1">
        <h3 className="text-body-lg font-medium text-on-surface">{title}</h3>
        {description && (
          <p className="text-body-md font-body-md text-on-surface-variant max-w-sm">{description}</p>
        )}
      </div>
      {action}
    </div>
  );
}

/** Error state that renders a normalized message plus an optional retry. */
export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const apiError = toApiError(error);
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-error-container text-on-error-container">
        <Icon name="error" className="text-3xl" />
      </div>
      <div className="space-y-1">
        <h3 className="text-body-lg font-medium text-on-surface">Something went wrong</h3>
        <p className="text-body-md font-body-md text-on-surface-variant max-w-sm">{apiError.message}</p>
      </div>
      {onRetry && (
        <Button variant="outline" size="sm" onClick={onRetry}>
          <Icon name="refresh" className="text-[18px]" />
          Try again
        </Button>
      )}
    </div>
  );
}
