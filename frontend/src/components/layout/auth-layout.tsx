import type { ReactNode } from "react";
import { Icon } from "@/components/ui/icon";

/**
 * Centered glass card used by all transactional auth screens (login, register, password reset).
 * Mirrors the Stitch login canvas: no nav shell, subtle blurred accent behind a rounded card.
 */
export function AuthLayout({
  title,
  subtitle,
  children,
  footer,
  wide,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
  wide?: boolean;
}) {
  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-background">
      <div
        className={`w-full ${wide ? "max-w-2xl" : "max-w-md"} bg-white/90 backdrop-blur rounded-3xl p-8 sm:p-10 relative overflow-hidden shadow-ambient border border-outline-variant/30`}
      >
        <div className="absolute top-0 right-0 -mr-20 -mt-20 w-64 h-64 bg-primary-fixed rounded-full blur-3xl opacity-30 pointer-events-none" />

        <div className="flex flex-col items-center mb-8 relative z-10">
          <div className="w-12 h-12 bg-primary-container rounded-xl flex items-center justify-center mb-4 text-white">
            <Icon name="work" className="text-3xl" filled />
          </div>
          <h1 className="text-headline-md font-headline-md text-on-surface text-center">{title}</h1>
          {subtitle && (
            <p className="text-body-md font-body-md text-on-surface-variant mt-2 text-center">
              {subtitle}
            </p>
          )}
        </div>

        <div className="relative z-10">{children}</div>

        {footer && <div className="mt-8 text-center relative z-10">{footer}</div>}
      </div>
    </div>
  );
}
