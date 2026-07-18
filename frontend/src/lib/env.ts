/** Centralised access to Vite environment configuration. */
export const env = {
  /** Base path/URL for the REST API. Proxied to the Spring Boot backend in dev. */
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "/api/v1",
  /** Google OAuth web client id. Empty string when unset — the Google button hides itself. */
  googleClientId: (import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined) ?? "",
} as const;
