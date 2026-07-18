import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { Toaster } from "sonner";
import "./index.css";
import { router } from "./app/router";
import { AuthProvider } from "./features/auth/auth-context";
import { queryClient } from "./lib/query-client";
import { configService } from "./services/config-service";
import { GoogleConfigProvider } from "./features/auth/google-config";

function App({ googleClientId }: { googleClientId: string }) {
  return (
    <GoogleConfigProvider clientId={googleClientId}>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <RouterProvider router={router} />
          <Toaster position="top-right" richColors />
        </AuthProvider>
      </QueryClientProvider>
    </GoogleConfigProvider>
  );
}

/**
 * The Google OAuth client id is served by the backend (GET /api/v1/public/config) rather than
 * baked into the build, so the frontend only needs its API base URL at build time. We fetch it
 * once before rendering; if the request fails or Google is disabled, the app runs password-only
 * and the Google button hides itself.
 */
async function bootstrap(): Promise<void> {
  let googleClientId = "";
  try {
    googleClientId = (await configService.getPublicConfig()).googleClientId;
  } catch {
    // Non-fatal: fall back to password-only login.
  }

  const app = <App googleClientId={googleClientId} />;

  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      {googleClientId ? (
        <GoogleOAuthProvider clientId={googleClientId}>{app}</GoogleOAuthProvider>
      ) : (
        app
      )}
    </StrictMode>,
  );
}

void bootstrap();
