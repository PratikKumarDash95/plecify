import { useContext } from "react";
import { AuthContext, type AuthState } from "./auth-context";

/** Access the auth session. Must be used within <AuthProvider>. */
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
