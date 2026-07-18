import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Icon } from "@/components/ui/icon";
import { useForgotPassword } from "@/features/auth/auth-hooks";
import { forgotPasswordSchema, type ForgotPasswordFormValues } from "@/features/auth/schemas";
import { paths } from "@/app/routes";
import { toApiError } from "@/lib/api-helpers";

export function ForgotPasswordPage() {
  const forgotPassword = useForgotPassword();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "" },
  });

  const onSubmit = async (values: ForgotPasswordFormValues) => {
    try {
      await forgotPassword.mutateAsync(values);
    } catch (err) {
      // The endpoint intentionally doesn't reveal whether an email exists; surface only real
      // (e.g. network) failures and otherwise fall through to the neutral success screen.
      const apiError = toApiError(err);
      if (apiError.status && apiError.status >= 500) {
        toast.error(apiError.message || "Something went wrong");
        return;
      }
    }
  };

  if (forgotPassword.isSuccess) {
    return (
      <AuthLayout title="Check your inbox" subtitle="If that email is registered, a reset link is on its way.">
        <div className="flex flex-col items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-full bg-primary-fixed text-on-primary-fixed-variant">
            <Icon name="mark_email_read" className="text-3xl" />
          </div>
          <Link to={paths.login} className="text-sm font-medium text-primary hover:text-on-primary-fixed-variant">
            Back to sign in
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Forgot password?" subtitle="Enter your email and we'll send a reset link.">
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
        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting || forgotPassword.isPending}>
          Send reset link
        </Button>
      </form>
      <div className="mt-6 text-center text-body-md font-body-md text-on-surface-variant">
        <Link to={paths.login} className="font-medium text-primary hover:text-on-primary-fixed-variant">
          Back to sign in
        </Link>
      </div>
    </AuthLayout>
  );
}
