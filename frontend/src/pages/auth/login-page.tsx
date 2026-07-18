import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";
import { GoogleSignInButton } from "@/features/auth/google-sign-in-button";
import {
  loginSchema,
  otpSchema,
  type LoginFormValues,
  type OtpFormValues,
} from "@/features/auth/schemas";
import { paths, roleHome } from "@/app/routes";
import { toApiError } from "@/lib/api-helpers";
import { useGoogleClientId } from "@/features/auth/google-config";
import type { UserResponse } from "@/types/auth";

export function LoginPage() {
  const { login, verifyOtp, resendOtp, loginWithGoogle } = useAuth();
  const googleClientId = useGoogleClientId();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname;

  // After step one succeeds we hold the email and swap the form for the OTP entry step.
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const otpForm = useForm<OtpFormValues>({
    resolver: zodResolver(otpSchema),
    defaultValues: { code: "" },
  });

  // Both OTP and Google flows land here once a session exists.
  const completeLogin = (user: UserResponse) => {
    toast.success(`Welcome back, ${user.fullName.split(" ")[0]}`);
    const destination = from ?? roleHome[user.roles[0]];
    navigate(destination, { replace: true });
  };

  const onSubmit = async (values: LoginFormValues) => {
    try {
      const challenge = await login(values);
      setPendingEmail(challenge.email);
      toast.success("We emailed you a 6-digit login code.");
    } catch (err) {
      const apiError = toApiError(err);
      toast.error(apiError.message || "Unable to sign in");
    }
  };

  const onVerifyOtp = async (values: OtpFormValues) => {
    if (!pendingEmail) return;
    try {
      completeLogin(await verifyOtp({ email: pendingEmail, code: values.code }));
    } catch (err) {
      const apiError = toApiError(err);
      toast.error(apiError.message || "Invalid or expired code");
    }
  };

  const onResendOtp = async () => {
    if (!pendingEmail) return;
    try {
      await resendOtp(pendingEmail);
      toast.success("A new code is on its way.");
    } catch (err) {
      const apiError = toApiError(err);
      toast.error(apiError.message || "Unable to resend code");
    }
  };

  const onGoogleCredential = async (idToken: string) => {
    try {
      completeLogin(await loginWithGoogle(idToken));
    } catch (err) {
      const apiError = toApiError(err);
      toast.error(apiError.message || "Unable to sign in with Google");
    }
  };

  if (pendingEmail) {
    return (
      <AuthLayout
        title="Enter your code"
        subtitle={`We sent a 6-digit code to ${pendingEmail}. It expires shortly.`}
      >
        <form className="space-y-5" onSubmit={otpForm.handleSubmit(onVerifyOtp)} noValidate>
          <Input
            label="Verification code"
            leadingIcon="lock"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            placeholder="123456"
            error={otpForm.formState.errors.code?.message}
            {...otpForm.register("code")}
          />

          <Button
            type="submit"
            className="w-full"
            size="lg"
            isLoading={otpForm.formState.isSubmitting}
          >
            Verify and sign in
          </Button>
        </form>

        <div className="mt-6 flex items-center justify-between text-body-md font-body-md text-on-surface-variant">
          <button
            type="button"
            onClick={onResendOtp}
            className="font-medium text-primary hover:text-on-primary-fixed-variant"
          >
            Resend code
          </button>
          <button
            type="button"
            onClick={() => setPendingEmail(null)}
            className="font-medium text-primary hover:text-on-primary-fixed-variant"
          >
            Use a different account
          </button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to Plecify to continue.">
      <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        <Input
          label="Email address"
          leadingIcon="mail"
          type="email"
          autoComplete="email"
          placeholder="name@university.edu"
          error={errors.email?.message}
          {...register("email")}
        />
        <Input
          label="Password"
          leadingIcon="lock"
          type="password"
          autoComplete="current-password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register("password")}
        />

        <div className="flex items-center justify-end">
          <Link
            to={paths.forgotPassword}
            className="text-sm font-medium text-primary hover:text-on-primary-fixed-variant transition-colors"
          >
            Forgot password?
          </Link>
        </div>

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting}>
          Sign in
        </Button>
      </form>

      {googleClientId && (
        <>
          <div className="my-6 flex items-center gap-4">
            <span className="h-px flex-1 bg-outline-variant" />
            <span className="text-sm text-on-surface-variant">or</span>
            <span className="h-px flex-1 bg-outline-variant" />
          </div>
          <GoogleSignInButton
            onCredential={onGoogleCredential}
            onError={() => toast.error("Google sign-in was cancelled or failed")}
          />
        </>
      )}

      <div className="mt-6 text-center text-body-md font-body-md text-on-surface-variant">
        Don't have an account?{" "}
        <Link
          to={paths.registerStudent}
          className="font-medium text-primary hover:text-on-primary-fixed-variant"
        >
          Register as a student
        </Link>{" "}
        or{" "}
        <Link
          to={paths.registerCompany}
          className="font-medium text-primary hover:text-on-primary-fixed-variant"
        >
          as a company
        </Link>
        .
      </div>
    </AuthLayout>
  );
}
