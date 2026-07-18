import type { ReactNode } from "react";

/** Standard page heading with an optional description and right-aligned actions. */
export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <div className="space-y-1">
        <h2 className="text-headline-md font-headline-md text-on-surface">{title}</h2>
        {description && (
          <p className="text-body-md font-body-md text-on-surface-variant">{description}</p>
        )}
      </div>
      {actions && <div className="flex items-center gap-2 shrink-0">{actions}</div>}
    </div>
  );
}
