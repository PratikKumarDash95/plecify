import { NavLink, useLocation } from "react-router-dom";
import { cn } from "@/lib/utils";
import { Icon } from "@/components/ui/icon";
import { navByRole } from "@/app/nav-config";
import type { Role } from "@/types/auth";

/**
 * Fixed left navigation, mirroring the Stitch SideNavBar. On mobile it slides in as an overlay
 * controlled by `open`/`onClose`.
 */
export function Sidebar({
  role,
  open,
  onClose,
}: {
  role: Role;
  open: boolean;
  onClose: () => void;
}) {
  const items = navByRole[role];
  const location = useLocation();

  return (
    <>
      {/* Mobile scrim */}
      {open && (
        <div
          className="fixed inset-0 bg-black/40 z-40 md:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}
      <aside
        className={cn(
          "bg-white h-screen w-64 fixed left-0 top-0 border-r border-outline-variant shadow-md flex flex-col py-6 gap-2 z-50 transition-transform duration-300",
          "md:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
        id="sidenav"
      >
        <div className="px-6 mb-8 flex items-center gap-3">
          <div className="w-10 h-10 bg-primary-container rounded-lg flex items-center justify-center text-on-primary-container">
            <Icon name="school" className="font-bold text-2xl" filled />
          </div>
          <div>
            <h2 className="text-primary text-xl font-headline-md font-semibold leading-tight">
              PlacementPro
            </h2>
            <p className="text-on-surface-variant text-xs uppercase tracking-wider">
              Enterprise Portal
            </p>
          </div>
        </div>

        <nav className="flex-1 px-2 space-y-1 overflow-y-auto">
          {items.map((item) => {
            const active = item.matchPrefix
              ? location.pathname.startsWith(item.to)
              : location.pathname === item.to;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={!item.matchPrefix}
                onClick={onClose}
                className={cn(
                  "flex items-center gap-3 px-4 py-3 rounded-lg mx-2 transition-all group",
                  active
                    ? "bg-primary-container text-on-primary-container font-semibold"
                    : "text-on-surface-variant hover:bg-surface-container-low",
                )}
              >
                <Icon
                  name={item.icon}
                  className={cn(
                    "transition-colors",
                    active ? "" : "text-outline group-hover:text-primary",
                  )}
                  filled={active}
                />
                <span
                  className={cn(
                    "text-label-md font-label-md transition-colors",
                    active ? "" : "group-hover:text-primary",
                  )}
                >
                  {item.label}
                </span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
    </>
  );
}
