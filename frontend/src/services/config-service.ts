import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse } from "@/types/api";

/** Non-secret runtime settings served by GET /api/v1/public/config. */
export interface PublicConfig {
  /** Google OAuth web client id, or empty string when Google sign-in is disabled. */
  googleClientId: string;
}

/** Fetches public runtime configuration the app needs before login. */
export const configService = {
  async getPublicConfig(): Promise<PublicConfig> {
    const { data } = await apiClient.get<ApiResponse<{ googleClientId: string | null }>>(
      "/public/config",
    );
    const payload = unwrap(data);
    return { googleClientId: payload.googleClientId ?? "" };
  },
};
