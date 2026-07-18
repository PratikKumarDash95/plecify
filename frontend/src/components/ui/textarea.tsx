import { forwardRef, useId } from "react";
import { cn } from "@/lib/utils";

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, label, error, hint, id, ...props }, ref) => {
    const generatedId = useId();
    const areaId = id ?? generatedId;
    const describedBy = error ? `${areaId}-error` : hint ? `${areaId}-hint` : undefined;

    return (
      <div className="w-full">
        {label && (
          <label htmlFor={areaId} className="block text-label-md font-label-md text-on-surface mb-1.5">
            {label}
          </label>
        )}
        <textarea
          ref={ref}
          id={areaId}
          aria-invalid={!!error}
          aria-describedby={describedBy}
          className={cn(
            "block w-full px-3 py-2.5 border rounded-lg text-body-md font-body-md text-on-surface placeholder-outline transition-all min-h-[96px]",
            "focus:outline-none focus:border-primary-container focus:ring-2 focus:ring-primary-container/20",
            error ? "border-error" : "border-surface-variant",
            className,
          )}
          {...props}
        />
        {error ? (
          <p id={`${areaId}-error`} className="mt-1.5 text-sm text-error">
            {error}
          </p>
        ) : hint ? (
          <p id={`${areaId}-hint`} className="mt-1.5 text-sm text-on-surface-variant">
            {hint}
          </p>
        ) : null}
      </div>
    );
  },
);
Textarea.displayName = "Textarea";
