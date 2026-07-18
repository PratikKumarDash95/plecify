// Central token store. Access + refresh tokens are persisted in localStorage so a page
// reload keeps the session; the axios layer reads/writes exclusively through this module so
// there is a single source of truth for auth state.

import type { UserResponse } from "@/types/auth";

const ACCESS_TOKEN_KEY = "ccp.accessToken";
const REFRESH_TOKEN_KEY = "ccp.refreshToken";
const USER_KEY = "ccp.user";

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },
  // The backend has no /me endpoint, so we cache the authenticated user to rehydrate the
  // session on reload. Tokens remain the source of truth for validity.
  getUser(): UserResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserResponse;
    } catch {
      return null;
    }
  },
  setUser(user: UserResponse): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
} as const;
