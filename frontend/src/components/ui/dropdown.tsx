import { useEffect, useRef, useState, type ReactNode } from "react";
import { cn } from "@/lib/utils";

/**
 * Minimal accessible dropdown: a trigger and a right-aligned menu that closes on outside click
 * or Escape. Kept dependency-free rather than pulling in a headless menu library.
 */
export function Dropdown({
  trigger,
  children,
  align = "right",
  menuClassName,
}: {
  trigger: (props: { open: boolean }) => ReactNode;
  children: ReactNode | ((close: () => void) => ReactNode);
  align?: "left" | "right";
  menuClassName?: string;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onClick = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onClick);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const close = () => setOpen(false);

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="menu"
        aria-expanded={open}
        className="focus:outline-none"
      >
        {trigger({ open })}
      </button>
      {open && (
        <div
          role="menu"
          className={cn(
            "absolute mt-2 min-w-[12rem] rounded-lg bg-white shadow-lg ring-1 ring-black/5 py-1 z-50",
            align === "right" ? "right-0" : "left-0",
            menuClassName,
          )}
        >
          {typeof children === "function" ? children(close) : children}
        </div>
      )}
    </div>
  );
}

export function DropdownItem({
  icon,
  children,
  onClick,
  destructive,
}: {
  icon?: string;
  children: ReactNode;
  onClick?: () => void;
  destructive?: boolean;
}) {
  return (
    <button
      type="button"
      role="menuitem"
      onClick={onClick}
      className={cn(
        "flex w-full items-center gap-3 px-4 py-2 text-left text-sm transition-colors",
        destructive
          ? "text-error hover:bg-error-container/50"
          : "text-on-surface hover:bg-surface-container-low hover:text-primary",
      )}
    >
      {icon && <span className="material-symbols-outlined text-[20px]">{icon}</span>}
      {children}
    </button>
  );
}
