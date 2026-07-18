import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";
import { GoogleSignInButton } from "@/features/auth/google-sign-in-button";
import { loginSchema, type LoginFormValues } from "@/features/auth/schemas";
import { paths, roleHome } from "@/app/routes";
import { toApiError } from "@/lib/api-helpers";
import { env } from "@/lib/env";

export function LoginPage() {
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  // Both password and Google flows land here on success.
  const completeLogin = (user: Awaited<ReturnType<typeof login>>) => {
    toast.success(`Welcome back, ${user.fullName.split(" ")[0]}`);
    const destination = from ?? roleHome[user.roles[0]];
    navigate(destination, { replace: true });
  };

  const onSubmit = async (values: LoginFormValues) => {
    try {
      completeLogin(await login(values));
    } catch (err) {
      const apiError = toApiError(err);
      toast.error(apiError.message || "Unable to sign in");
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

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to PlacementPro to continue.">
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

      {env.googleClientId && (
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
