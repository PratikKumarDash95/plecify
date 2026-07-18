import { Icon } from "./icon";
import { cn } from "@/lib/utils";

// Bento-style metric tile from the Stitch dashboards: white surface, ambient shadow, an accent
// icon chip, a large value and a caption.
export function StatCard({
  label,
  value,
  icon,
  accent = "primary",
  hint,
  className,
}: {
  label: string;
  value: React.ReactNode;
  icon: string;
  accent?: "primary" | "success" | "warning" | "danger" | "tertiary";
  hint?: string;
  className?: string;
}) {
  const accentClasses: Record<string, string> = {
    primary: "bg-primary-fixed text-on-primary-fixed-variant",
    success: "bg-[#c6f0d8] text-[#0f5132]",
    warning: "bg-[#fdecc8] text-[#8a5a00]",
    danger: "bg-error-container text-on-error-container",
    tertiary: "bg-tertiary-fixed text-on-tertiary-fixed",
  };

  return (
    <div
      className={cn(
        "bg-white p-5 rounded-2xl shadow-ambient border border-outline-variant/20 flex flex-col gap-3",
        className,
      )}
    >
      <div className="flex items-center justify-between">
        <span className="text-label-md font-label-md text-on-surface-variant">{label}</span>
        <div className={cn("h-9 w-9 rounded-lg flex items-center justify-center", accentClasses[accent])}>
          <Icon name={icon} className="text-[20px]" />
        </div>
      </div>
      <div className="text-3xl font-headline-md font-semibold text-on-surface">{value}</div>
      {hint && <p className="text-xs text-on-surface-variant">{hint}</p>}
    </div>
  );
}
