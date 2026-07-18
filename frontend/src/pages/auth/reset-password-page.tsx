import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useResetPassword } from "@/features/auth/auth-hooks";
import { resetPasswordSchema, type ResetPasswordFormValues } from "@/features/auth/schemas";
import { paths } from "@/app/routes";
import { toApiError } from "@/lib/api-helpers";

export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const navigate = useNavigate();
  const resetPassword = useResetPassword();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { newPassword: "", confirmPassword: "" },
  });

  const onSubmit = async (values: ResetPasswordFormValues) => {
    try {
      await resetPassword.mutateAsync({ token, newPassword: values.newPassword });
      toast.success("Password updated. You can now sign in.");
      navigate(paths.login, { replace: true });
    } catch (err) {
      toast.error(toApiError(err).message || "Unable to reset password");
    }
  };

  if (!token) {
    return (
      <AuthLayout title="Invalid reset link" subtitle="This link is missing its token or has expired.">
        <div className="text-center">
          <Link to={paths.forgotPassword} className="font-medium text-primary hover:text-on-primary-fixed-variant">
            Request a new link
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Set a new password" subtitle="Choose a strong password you haven't used before.">
      <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        <Input
          label="New password"
          leadingIcon="lock"
          type="password"
          autoComplete="new-password"
          error={errors.newPassword?.message}
          {...register("newPassword")}
        />
        <Input
          label="Confirm new password"
          leadingIcon="lock"
          type="password"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />
        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting || resetPassword.isPending}>
          Update password
        </Button>
      </form>
    </AuthLayout>
  );
}
