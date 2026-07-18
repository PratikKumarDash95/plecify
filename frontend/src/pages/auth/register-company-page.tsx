import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { useRegisterCompany } from "@/features/auth/auth-hooks";
import { companyRegisterSchema, type CompanyRegisterFormValues } from "@/features/auth/schemas";
import { paths } from "@/app/routes";
import { toApiError, applyServerViolations } from "@/lib/api-helpers";
import type { RegisterCompanyRequest } from "@/types/auth";

export function RegisterCompanyPage() {
  const navigate = useNavigate();
  const registerCompany = useRegisterCompany();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CompanyRegisterFormValues>({
    resolver: zodResolver(companyRegisterSchema),
  });

  const onSubmit = async (values: CompanyRegisterFormValues) => {
    const payload: RegisterCompanyRequest = {
      email: values.email,
      password: values.password,
      contactPersonName: values.contactPersonName,
      phone: values.phone || undefined,
      companyName: values.companyName,
      industry: values.industry || undefined,
      website: values.website || undefined,
      description: values.description || undefined,
      headquarters: values.headquarters || undefined,
    };
    try {
      await registerCompany.mutateAsync(payload);
      toast.success("Company account created. Check your email to verify your address.");
      navigate(paths.login, { replace: true });
    } catch (err) {
      const handled = applyServerViolations(err, setError);
      if (!handled) toast.error(toApiError(err).message || "Registration failed");
    }
  };

  return (
    <AuthLayout
      title="Register your company"
      subtitle="Post jobs and reach eligible candidates across campuses."
      wide
    >
      <form className="space-y-6" onSubmit={handleSubmit(onSubmit)} noValidate>
        <section className="space-y-4">
          <h2 className="text-label-md font-label-md font-semibold text-on-surface-variant uppercase tracking-wide">
            Company
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Company name" leadingIcon="domain" error={errors.companyName?.message} {...register("companyName")} />
            <Input label="Industry (optional)" leadingIcon="category" error={errors.industry?.message} {...register("industry")} />
            <Input label="Website (optional)" leadingIcon="language" placeholder="https://" error={errors.website?.message} {...register("website")} />
            <Input label="Headquarters (optional)" leadingIcon="location_on" error={errors.headquarters?.message} {...register("headquarters")} />
          </div>
          <Textarea label="Description (optional)" rows={3} error={errors.description?.message} {...register("description")} />
        </section>

        <section className="space-y-4">
          <h2 className="text-label-md font-label-md font-semibold text-on-surface-variant uppercase tracking-wide">
            Primary contact & login
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Contact person" leadingIcon="person" error={errors.contactPersonName?.message} {...register("contactPersonName")} />
            <Input label="Phone (optional)" leadingIcon="call" error={errors.phone?.message} {...register("phone")} />
            <Input label="Email address" leadingIcon="mail" type="email" autoComplete="email" error={errors.email?.message} {...register("email")} />
            <div className="hidden sm:block" />
            <Input label="Password" leadingIcon="lock" type="password" autoComplete="new-password" error={errors.password?.message} {...register("password")} />
            <Input label="Confirm password" leadingIcon="lock" type="password" autoComplete="new-password" error={errors.confirmPassword?.message} {...register("confirmPassword")} />
          </div>
        </section>

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting || registerCompany.isPending}>
          Create company account
        </Button>
      </form>

      <div className="mt-6 text-center text-body-md font-body-md text-on-surface-variant">
        Already registered?{" "}
        <Link to={paths.login} className="font-medium text-primary hover:text-on-primary-fixed-variant">
          Sign in
        </Link>
      </div>
    </AuthLayout>
  );
}
