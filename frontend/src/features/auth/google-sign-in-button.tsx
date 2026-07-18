import { GoogleLogin } from "@react-oauth/google";
import { useGoogleClientId } from "./google-config";

/**
 * Renders Google's official "Sign in with Google" button. Hidden entirely when no client id is
 * available (served at runtime from the backend), so builds without OAuth just show password login.
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
  const googleClientId = useGoogleClientId();
  if (!googleClientId) {
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
