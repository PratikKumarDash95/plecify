import { cn } from "@/lib/utils";

/** Derives up-to-two-letter initials from a full name. */
function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function Avatar({ name, className }: { name: string; className?: string }) {
  return (
    <div
      className={cn(
        "flex items-center justify-center rounded-full bg-primary-fixed text-on-primary-fixed-variant font-semibold h-10 w-10 text-sm shrink-0",
        className,
      )}
      aria-hidden="true"
    >
      {initials(name)}
    </div>
  );
}
