import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import { env } from "./env";
import { tokenStorage } from "./token-storage";
import type { ApiResponse } from "@/types/api";

/**
 * Shared axios instance. Responsibilities:
 *  - attach the bearer access token to every request
 *  - on a 401, transparently refresh the token pair once and replay the request
 *  - serialise concurrent refreshes so we only hit /auth/refresh a single time
 *  - hard-logout (clear tokens + notify listeners) when refresh itself fails
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { "Content-Type": "application/json" },
});

// --- session-expiry broadcast -------------------------------------------------
// The auth layer subscribes to this so it can drop React state and redirect to /login
// without this low-level module importing React or the router.
type LogoutListener = () => void;
const logoutListeners = new Set<LogoutListener>();

export function onForcedLogout(listener: LogoutListener): () => void {
  logoutListeners.add(listener);
  return () => logoutListeners.delete(listener);
}

function emitForcedLogout(): void {
  tokenStorage.clear();
  logoutListeners.forEach((fn) => fn());
}

// --- request interceptor ------------------------------------------------------
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- response interceptor: single-flight refresh ------------------------------
interface RetriableConfig extends AxiosRequestConfig {
  _retry?: boolean;
}

let isRefreshing = false;
let pendingQueue: Array<(token: string | null) => void> = [];

function flushQueue(token: string | null): void {
  pendingQueue.forEach((resolve) => resolve(token));
  pendingQueue = [];
}

async function refreshTokens(): Promise<string | null> {
  const refreshToken = tokenStorage.getRefreshToken();
  if (!refreshToken) return null;
  try {
    // Use a bare axios call so this request skips the interceptors below.
    const { data } = await axios.post<
      ApiResponse<{ accessToken: string; refreshToken: string; tokenType: string; expiresIn: number }>
    >(`${env.apiBaseUrl}/auth/refresh`, { refreshToken });
    const payload = data.data;
    if (!payload) return null;
    tokenStorage.setTokens(payload.accessToken, payload.refreshToken);
    return payload.accessToken;
  } catch {
    return null;
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined;
    const status = error.response?.status;

    // Only attempt a refresh for genuine auth failures, once per request, and never for
    // the refresh/login endpoints themselves.
    const isAuthEndpoint = original?.url?.includes("/auth/");
    if (status !== 401 || !original || original._retry || isAuthEndpoint) {
      return Promise.reject(error);
    }

    original._retry = true;

    if (isRefreshing) {
      // A refresh is already in flight — queue until it resolves, then replay.
      return new Promise((resolve, reject) => {
        pendingQueue.push((token) => {
          if (!token) {
            reject(error);
            return;
          }
          original.headers = { ...original.headers, Authorization: `Bearer ${token}` };
          resolve(apiClient(original));
        });
      });
    }

    isRefreshing = true;
    const newToken = await refreshTokens();
    isRefreshing = false;
    flushQueue(newToken);

    if (!newToken) {
      emitForcedLogout();
      return Promise.reject(error);
    }

    original.headers = { ...original.headers, Authorization: `Bearer ${newToken}` };
    return apiClient(original);
  },
);
