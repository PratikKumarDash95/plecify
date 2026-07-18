import { forwardRef } from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

// Variants derived from the Stitch "Enterprise SaaS Clarity" buttons: primary uses
// bg-primary-container with the on-primary-fixed-variant hover, matching login/code.html.
const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-label-md font-label-md transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-container disabled:pointer-events-none disabled:opacity-50",
  {
    variants: {
      variant: {
        primary:
          "bg-primary-container text-white shadow-sm hover:bg-on-primary-fixed-variant border border-transparent",
        secondary:
          "bg-secondary-container text-on-secondary-container hover:bg-secondary-fixed border border-transparent",
        outline:
          "border border-surface-variant bg-white text-on-surface hover:bg-surface-container-low",
        ghost: "bg-transparent text-on-surface hover:bg-surface-container-low",
        danger: "bg-error text-on-error hover:bg-on-error-container border border-transparent",
        link: "bg-transparent text-primary hover:text-on-primary-fixed-variant underline-offset-4 hover:underline p-0 h-auto",
      },
      size: {
        sm: "h-9 px-3 text-sm",
        md: "h-11 px-4 py-2.5",
        lg: "h-12 px-6 py-3",
        icon: "h-10 w-10 p-0",
      },
    },
    defaultVariants: {
      variant: "primary",
      size: "md",
    },
  },
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  isLoading?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, isLoading, disabled, children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(buttonVariants({ variant, size }), className)}
        disabled={disabled || isLoading}
        {...props}
      >
        {isLoading && (
          <span
            className="material-symbols-outlined animate-spin text-[20px]"
            aria-hidden="true"
          >
            progress_activity
          </span>
        )}
        {children}
      </button>
    );
  },
);
Button.displayName = "Button";
