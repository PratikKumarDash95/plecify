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
import { env } from "./lib/env";

const app = (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster position="top-right" richColors />
    </AuthProvider>
  </QueryClientProvider>
);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    {/* Only mount the Google provider when a client id is configured; otherwise the app runs
        password-only and the Google button hides itself. */}
    {env.googleClientId ? (
      <GoogleOAuthProvider clientId={env.googleClientId}>{app}</GoogleOAuthProvider>
    ) : (
      app
    )}
  </StrictMode>,
);
