import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Icon } from "@/components/ui/icon";
import { Avatar } from "@/components/ui/avatar";
import { Dropdown, DropdownItem } from "@/components/ui/dropdown";
import { useAuth } from "@/features/auth/use-auth";
import { roleDisplayName } from "@/app/nav-config";
import { paths } from "@/app/routes";

/** Sticky contextual top bar: page title, mobile menu toggle, and the user menu. */
export function Topbar({ title, onMenuClick }: { title: string; onMenuClick: () => void }) {
  const { user, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
      // logout is best-effort; local tokens are cleared regardless.
    } finally {
      toast.success("Signed out");
      navigate(paths.login, { replace: true });
    }
  };

  return (
    <header className="bg-surface/80 backdrop-blur-md sticky top-0 z-30 border-b border-outline-variant/30 px-4 sm:px-6 py-4 flex justify-between items-center h-20">
      <div className="flex items-center gap-4 min-w-0">
        <button
          className="md:hidden text-on-surface p-2 rounded-lg hover:bg-surface-container-low transition-colors"
          onClick={onMenuClick}
          aria-label="Open navigation"
        >
          <Icon name="menu" />
        </button>
        <h1 className="text-headline-md font-headline-md text-on-surface text-xl sm:text-2xl truncate">
          {title}
        </h1>
      </div>

      <div className="flex items-center gap-2 sm:gap-4">
        <Dropdown
          trigger={() => (
            <div className="flex items-center gap-3 pl-2 sm:pl-4 sm:border-l border-outline-variant/50 cursor-pointer hover:opacity-80 transition-opacity">
              <div className="hidden sm:block text-right">
                <p className="text-label-md font-label-md font-semibold text-on-surface leading-tight">
                  {user?.fullName ?? "Account"}
                </p>
                <p className="text-xs text-on-surface-variant">
                  {role ? roleDisplayName[role] : ""}
                </p>
              </div>
              <Avatar name={user?.fullName ?? "?"} />
            </div>
          )}
        >
          <div className="px-4 py-2 border-b border-outline-variant/30 sm:hidden">
            <p className="text-label-md font-semibold text-on-surface">{user?.fullName}</p>
            <p className="text-xs text-on-surface-variant truncate">{user?.email}</p>
          </div>
          <DropdownItem icon="logout" destructive onClick={handleLogout}>
            Sign out
          </DropdownItem>
        </Dropdown>
      </div>
    </header>
  );
}
