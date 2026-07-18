import { useEffect, useRef } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { Spinner } from "@/components/ui/spinner";
import { useVerifyEmail } from "@/features/auth/auth-hooks";
import { paths } from "@/app/routes";
import { toApiError } from "@/lib/api-helpers";

export function VerifyEmailPage() {
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const verifyEmail = useVerifyEmail();
  const attempted = useRef(false);

  useEffect(() => {
    // StrictMode double-invokes effects in dev; guard so the one-time token isn't spent twice.
    if (token && !attempted.current) {
      attempted.current = true;
      verifyEmail.mutate(token);
    }
  }, [token, verifyEmail]);

  if (!token) {
    return (
      <AuthLayout title="Missing verification token" subtitle="This link is incomplete.">
        <div className="text-center">
          <Link to={paths.login} className="font-medium text-primary hover:text-on-primary-fixed-variant">
            Back to sign in
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Email verification">
      <div className="flex flex-col items-center gap-4 text-center">
        {verifyEmail.isPending && (
          <>
            <Spinner className="text-4xl" />
            <p className="text-body-md text-on-surface-variant">Verifying your email…</p>
          </>
        )}
        {verifyEmail.isSuccess && (
          <>
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-[#c6f0d8] text-[#0f5132]">
              <Icon name="check_circle" className="text-3xl" />
            </div>
            <p className="text-body-md text-on-surface">
              {verifyEmail.data || "Your email has been verified."}
            </p>
            <Link to={paths.login}>
              <Button>Continue to sign in</Button>
            </Link>
          </>
        )}
        {verifyEmail.isError && (
          <>
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-error-container text-on-error-container">
              <Icon name="error" className="text-3xl" />
            </div>
            <p className="text-body-md text-on-surface">
              {toApiError(verifyEmail.error).message || "Verification failed or the link expired."}
            </p>
            <Link to={paths.login} className="font-medium text-primary hover:text-on-primary-fixed-variant">
              Back to sign in
            </Link>
          </>
        )}
      </div>
    </AuthLayout>
  );
}
