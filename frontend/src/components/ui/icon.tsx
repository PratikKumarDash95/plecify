import { cn } from "@/lib/utils";

interface IconProps {
  /** Material Symbols Outlined ligature name, e.g. "work", "mail", "lock". */
  name: string;
  className?: string;
  /** Render the filled variant. */
  filled?: boolean;
  title?: string;
}

/** Thin wrapper over the Material Symbols Outlined icon font used throughout the designs. */
export function Icon({ name, className, filled, title }: IconProps) {
  return (
    <span
      className={cn("material-symbols-outlined select-none", className)}
      style={filled ? { fontVariationSettings: "'FILL' 1" } : undefined}
      aria-hidden={title ? undefined : true}
      role={title ? "img" : undefined}
      aria-label={title}
    >
      {name}
    </span>
  );
}
