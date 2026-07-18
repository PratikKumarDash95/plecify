import { useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { Sidebar } from "./sidebar";
import { Topbar } from "./topbar";
import { ChatbotWidget } from "@/features/ai/chatbot-widget";
import { useAuth } from "@/features/auth/use-auth";
import { useIdleLogout } from "@/features/auth/use-idle-logout";
import { navByRole } from "@/app/nav-config";
import type { Role } from "@/types/auth";

/** Resolves the top-bar title from the deepest matching nav item for the current path. */
function usePageTitle(role: Role): string {
  const location = useLocation();
  const items = navByRole[role];
  // Prefer the longest matching prefix so nested routes still resolve a title.
  const match = [...items]
    .sort((a, b) => b.to.length - a.to.length)
    .find((item) =>
      item.matchPrefix ? location.pathname.startsWith(item.to) : location.pathname === item.to,
    );
  return match?.label ?? "PlacementPro";
}

export function DashboardLayout() {
  const { role } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  // role is guaranteed non-null here because this layout is always inside ProtectedRoute.
  const effectiveRole = (role ?? "STUDENT") as Role;
  const title = usePageTitle(effectiveRole);

  // Students get an auto sign-out after 17 minutes of inactivity.
  useIdleLogout(effectiveRole === "STUDENT");

  return (
    <div className="bg-background text-on-background min-h-screen flex">
      <Sidebar role={effectiveRole} open={mobileOpen} onClose={() => setMobileOpen(false)} />
      <div className="flex-1 md:ml-64 flex flex-col min-h-screen min-w-0">
        <Topbar title={title} onMenuClick={() => setMobileOpen(true)} />
        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full">
          <Outlet />
        </main>
      </div>
      <ChatbotWidget />
    </div>
  );
}
