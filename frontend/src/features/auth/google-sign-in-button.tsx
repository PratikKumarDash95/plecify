import { GoogleLogin } from "@react-oauth/google";
import { env } from "@/lib/env";

/**
 * Renders Google's official "Sign in with Google" button. Hidden entirely when no client id is
 * configured (VITE_GOOGLE_CLIENT_ID), so local/dev builds without OAuth just show password login.
 *
 * The parent supplies onCredential with the ID token that Google returns; exchanging it for our
 * own session is the caller's concern (see the login page).
 */
export function GoogleSignInButton({
  onCredential,
  onError,
}: {
  onCredential: (idToken: string) => void;
  onError?: () => void;
}) {
  if (!env.googleClientId) {
    return null;
  }

  return (
    <div className="flex justify-center">
      <GoogleLogin
        onSuccess={(response) => {
          if (response.credential) {
            onCredential(response.credential);
          } else {
            onError?.();
          }
        }}
        onError={() => onError?.()}
        theme="outline"
        size="large"
        width="360"
        text="continue_with"
        shape="rectangular"
      />
    </div>
  );
}
