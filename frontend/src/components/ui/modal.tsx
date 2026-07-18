import { useEffect, type ReactNode } from "react";
import { cn } from "@/lib/utils";
import { Icon } from "./icon";

/** Accessible modal dialog with scrim, Escape-to-close, and body scroll lock. */
export function Modal({
  open,
  onClose,
  title,
  children,
  footer,
  size = "md",
}: {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: "sm" | "md" | "lg";
}) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  if (!open) return null;

  const sizes = { sm: "max-w-md", md: "max-w-lg", lg: "max-w-2xl" };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} aria-hidden="true" />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className={cn(
          "relative z-10 w-full bg-white rounded-2xl shadow-ambient max-h-[90vh] flex flex-col",
          sizes[size],
        )}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-outline-variant/40">
          <h2 className="text-headline-md font-headline-md text-on-surface text-xl">{title}</h2>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-on-surface-variant hover:bg-surface-container-low transition-colors"
            aria-label="Close"
          >
            <Icon name="close" />
          </button>
        </div>
        <div className="px-6 py-5 overflow-y-auto">{children}</div>
        {footer && (
          <div className="px-6 py-4 border-t border-outline-variant/40 flex justify-end gap-3">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
