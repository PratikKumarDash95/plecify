import { forwardRef, useId, useState } from "react";
import { cn } from "@/lib/utils";
import { Icon } from "./icon";

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  /** Leading Material Symbols icon name (e.g. "mail"). */
  leadingIcon?: string;
  error?: string;
  hint?: string;
}

// Matches the Stitch input: pl-10 when an icon is present, rounded-lg border-surface-variant,
// focus ring via the .input-glow rule (recreated here with Tailwind focus utilities).
export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, leadingIcon, error, hint, id, type = "text", ...props }, ref) => {
    const generatedId = useId();
    const inputId = id ?? generatedId;
    const isPassword = type === "password";
    const [revealed, setRevealed] = useState(false);
    const effectiveType = isPassword && revealed ? "text" : type;
    const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined;

    return (
      <div className="w-full">
        {label && (
          <label htmlFor={inputId} className="block text-label-md font-label-md text-on-surface mb-1.5">
            {label}
          </label>
        )}
        <div className="relative">
          {leadingIcon && (
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Icon name={leadingIcon} className="text-outline" />
            </div>
          )}
          <input
            ref={ref}
            id={inputId}
            type={effectiveType}
            aria-invalid={!!error}
            aria-describedby={describedBy}
            className={cn(
              "block w-full py-2.5 border rounded-lg text-body-md font-body-md text-on-surface placeholder-outline transition-all",
              "focus:outline-none focus:border-primary-container focus:ring-2 focus:ring-primary-container/20",
              leadingIcon ? "pl-10" : "pl-3",
              isPassword ? "pr-10" : "pr-3",
              error ? "border-error focus:border-error focus:ring-error/20" : "border-surface-variant",
              className,
            )}
            {...props}
          />
          {isPassword && (
            <button
              type="button"
              onClick={() => setRevealed((v) => !v)}
              className="absolute inset-y-0 right-0 pr-3 flex items-center text-outline hover:text-on-surface transition-colors"
              aria-label={revealed ? "Hide password" : "Show password"}
              tabIndex={-1}
            >
              <Icon name={revealed ? "visibility_off" : "visibility"} />
            </button>
          )}
        </div>
        {error ? (
          <p id={`${inputId}-error`} className="mt-1.5 text-sm text-error">
            {error}
          </p>
        ) : hint ? (
          <p id={`${inputId}-hint`} className="mt-1.5 text-sm text-on-surface-variant">
            {hint}
          </p>
        ) : null}
      </div>
    );
  },
);
Input.displayName = "Input";
