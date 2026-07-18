import { cn } from "@/lib/utils";
import { Icon } from "./icon";

export function Spinner({ className }: { className?: string }) {
  return (
    <Icon
      name="progress_activity"
      className={cn("animate-spin text-primary-container text-2xl", className)}
      title="Loading"
    />
  );
}

/** Full-area centered loading state for pages and cards. */
export function LoadingState({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-on-surface-variant">
      <Spinner className="text-3xl" />
      <p className="text-body-md font-body-md">{label}</p>
    </div>
  );
}
