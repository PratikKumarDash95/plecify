import { createContext, useContext } from "react";

/**
 * Holds the runtime Google OAuth client id fetched from the backend. Empty string means Google
 * sign-in is disabled, in which case {@link GoogleOAuthProvider} is not mounted and the
 * sign-in button hides itself.
 */
const GoogleClientIdContext = createContext<string>("");

export function GoogleConfigProvider({
  clientId,
  children,
}: {
  clientId: string;
  children: React.ReactNode;
}) {
  return <GoogleClientIdContext.Provider value={clientId}>{children}</GoogleClientIdContext.Provider>;
}

/** Returns the configured Google client id, or "" when Google sign-in is unavailable. */
export function useGoogleClientId(): string {
  return useContext(GoogleClientIdContext);
}
