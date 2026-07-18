import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { authService } from "@/services/auth-service";
import { onForcedLogout } from "@/lib/api-client";
import { tokenStorage } from "@/lib/token-storage";
import { isTokenExpired } from "@/lib/jwt";


import type {
  LoginChallengeResponse,
  LoginRequest,
  LoginResponse,
  Role,
  UserResponse,
  VerifyOtpRequest,
} from "@/types/auth";

export interface AuthState {
  user: UserResponse | null;
  role: Role | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  /** Step 1: verify credentials. Emails an OTP and returns the challenge; no session yet. */
  login: (payload: LoginRequest) => Promise<LoginChallengeResponse>;
  /** Step 2: exchange the emailed OTP for a session. */
  verifyOtp: (payload: VerifyOtpRequest) => Promise<UserResponse>;
  /** Re-sends the login OTP for an in-progress two-step login. */
  resendOtp: (email: string) => Promise<void>;
  /** Exchanges a Google ID token (credential) for a session. */
  loginWithGoogle: (idToken: string) => Promise<UserResponse>;
  logout: () => Promise<void>;
  /** Locally clears session state without a server round-trip (used by forced logout). */
  clearSession: () => void;
}

export const AuthContext = createContext<AuthState | null>(null);

/** Derives the primary role: prefer the explicit login `role`, else the first granted role. */
function primaryRole(user: UserResponse | null): Role | null {
  if (!user) return null;
  return user.roles[0] ?? null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  // Rehydrate from storage on mount. A present, non-expired access token (or a refresh token,
  // since the axios layer can silently refresh) plus a cached user means we're still logged in.
  useEffect(() => {
    const cachedUser = tokenStorage.getUser();
    const accessToken = tokenStorage.getAccessToken();
    const refreshToken = tokenStorage.getRefreshToken();
    const hasUsableSession = cachedUser && (!isTokenExpired(accessToken) || !!refreshToken);
    if (hasUsableSession) {
      setUser(cachedUser);
    } else {
      tokenStorage.clear();
    }
    setIsInitializing(false);
  }, []);

  const clearSession = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
  }, []);

  // When the axios layer fails to refresh, it broadcasts a forced logout. Drop local state.
  useEffect(() => onForcedLogout(clearSession), [clearSession]);

  // Shared post-login bookkeeping: persist tokens + user, then update React state.
  const establishSession = useCallback((result: LoginResponse) => {
    tokenStorage.setTokens(result.accessToken, result.refreshToken);
    tokenStorage.setUser(result.user);
    setUser(result.user);
    return result.user;
  }, []);

  // Step 1 does not establish a session — it only triggers the OTP email.
  const login = useCallback((payload: LoginRequest) => authService.login(payload), []);

  const verifyOtp = useCallback(
    async (payload: VerifyOtpRequest) => establishSession(await authService.verifyOtp(payload)),
    [establishSession],
  );

  const resendOtp = useCallback(async (email: string) => {
    await authService.resendOtp({ email });
  }, []);

  const loginWithGoogle = useCallback(
    async (idToken: string) => establishSession(await authService.googleLogin({ idToken })),
    [establishSession],
  );

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      user,
      role: primaryRole(user),
      isAuthenticated: !!user,
      isInitializing,
      login,
      verifyOtp,
      resendOtp,
      loginWithGoogle,
      logout,
      clearSession,
    }),
    [user, isInitializing, login, verifyOtp, resendOtp, loginWithGoogle, logout, clearSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
