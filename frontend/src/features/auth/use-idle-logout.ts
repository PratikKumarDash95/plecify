import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { useAuth } from "@/features/auth/use-auth";
import { paths } from "@/app/routes";

/** Log the user out after this many minutes without interaction. */
const DEFAULT_IDLE_MINUTES = 17;

// Interaction signals that count as "the user is still here". Passive listeners keep scrolling smooth.
const ACTIVITY_EVENTS: (keyof WindowEventMap)[] = [
  "mousemove",
  "mousedown",
  "keydown",
  "touchstart",
  "scroll",
  "wheel",
];

/**
 * Signs the user out after a period of inactivity. Any tracked interaction resets the timer.
 * Intended for sensitive sessions (e.g. students) — mount it once inside an authenticated layout.
 */
export function useIdleLogout(enabled: boolean, idleMinutes: number = DEFAULT_IDLE_MINUTES) {
  const { logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!enabled || !isAuthenticated) return;

    const idleMs = idleMinutes * 60 * 1000;

    const expire = async () => {
      // A hidden tab firing its timer shouldn't log out a user who's active in another tab;
      // only expire when this document is the visible one.
      if (document.visibilityState === "hidden") return;
      try {
        await logout();
      } finally {
        toast.info("You were signed out due to inactivity.");
        navigate(paths.login, { replace: true });
      }
    };

    const reset = () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(expire, idleMs);
    };

    reset();
    ACTIVITY_EVENTS.forEach((event) =>
      window.addEventListener(event, reset, { passive: true }),
    );

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      ACTIVITY_EVENTS.forEach((event) => window.removeEventListener(event, reset));
    };
  }, [enabled, idleMinutes, isAuthenticated, logout, navigate]);
}
