import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium whitespace-nowrap",
  {
    variants: {
      variant: {
        neutral: "bg-surface-container-high text-on-surface-variant",
        primary: "bg-primary-fixed text-on-primary-fixed-variant",
        success: "bg-[#c6f0d8] text-[#0f5132]",
        warning: "bg-[#fdecc8] text-[#8a5a00]",
        danger: "bg-error-container text-on-error-container",
        info: "bg-secondary-container text-on-secondary-container",
        tertiary: "bg-tertiary-fixed text-on-tertiary-fixed",
      },
    },
    defaultVariants: {
      variant: "neutral",
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

export function Badge({ className, variant, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ variant }), className)} {...props} />;
}
