import { forwardRef, useId } from "react";
import { cn } from "@/lib/utils";
import { Icon } from "./icon";

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  hint?: string;
  options: SelectOption[];
  placeholder?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, label, error, hint, options, placeholder, id, ...props }, ref) => {
    const generatedId = useId();
    const selectId = id ?? generatedId;
    const describedBy = error ? `${selectId}-error` : hint ? `${selectId}-hint` : undefined;

    return (
      <div className="w-full">
        {label && (
          <label htmlFor={selectId} className="block text-label-md font-label-md text-on-surface mb-1.5">
            {label}
          </label>
        )}
        <div className="relative">
          <select
            ref={ref}
            id={selectId}
            aria-invalid={!!error}
            aria-describedby={describedBy}
            className={cn(
              "block w-full appearance-none py-2.5 pl-3 pr-10 border rounded-lg text-body-md font-body-md text-on-surface bg-white transition-all",
              "focus:outline-none focus:border-primary-container focus:ring-2 focus:ring-primary-container/20",
              error ? "border-error" : "border-surface-variant",
              className,
            )}
            {...props}
          >
            {placeholder && (
              <option value="" disabled>
                {placeholder}
              </option>
            )}
            {options.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <div className="absolute inset-y-0 right-0 pr-2 flex items-center pointer-events-none">
            <Icon name="expand_more" className="text-outline" />
          </div>
        </div>
        {error ? (
          <p id={`${selectId}-error`} className="mt-1.5 text-sm text-error">
            {error}
          </p>
        ) : hint ? (
          <p id={`${selectId}-hint`} className="mt-1.5 text-sm text-on-surface-variant">
            {hint}
          </p>
        ) : null}
      </div>
    );
  },
);
Select.displayName = "Select";
