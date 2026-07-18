import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./use-auth";
import { roleHome, paths } from "@/app/routes";
import { LoadingState } from "@/components/ui/spinner";
import type { Role } from "@/types/auth";

/**
 * Guards a route subtree. Redirects unauthenticated users to /login (preserving the intended
 * destination) and users whose role isn't permitted to their own role's home.
 */
export function ProtectedRoute({ allow }: { allow?: Role[] }) {
  const { isAuthenticated, isInitializing, role } = useAuth();
  const location = useLocation();

  if (isInitializing) {
    return <LoadingState label="Restoring your session…" />;
  }

  if (!isAuthenticated) {
    return <Navigate to={paths.login} replace state={{ from: location }} />;
  }

  if (allow && role && !allow.includes(role)) {
    return <Navigate to={roleHome[role]} replace />;
  }

  return <Outlet />;
}
