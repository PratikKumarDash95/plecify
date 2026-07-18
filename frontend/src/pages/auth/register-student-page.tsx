import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { AuthLayout } from "@/components/layout/auth-layout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useRegisterStudent } from "@/features/auth/auth-hooks";
import {
  studentRegisterSchema,
  type StudentRegisterFormValues,
  type StudentRegisterFormInput,
} from "@/features/auth/schemas";
import { paths } from "@/app/routes";
import { toApiError, applyServerViolations } from "@/lib/api-helpers";
import type { RegisterStudentRequest } from "@/types/auth";

export function RegisterStudentPage() {
  const navigate = useNavigate();
  const registerStudent = useRegisterStudent();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<StudentRegisterFormInput, unknown, StudentRegisterFormValues>({
    resolver: zodResolver(studentRegisterSchema),
    defaultValues: {
      activeBacklogs: 0,
      totalBacklogs: 0,
    } as Partial<StudentRegisterFormInput> as StudentRegisterFormInput,
  });

  const onSubmit = async (values: StudentRegisterFormValues) => {
    const payload: RegisterStudentRequest = {
      email: values.email,
      password: values.password,
      fullName: values.fullName,
      phone: values.phone || undefined,
      universityDomain: values.universityDomain,
      rollNumber: values.rollNumber,
      department: values.department,
      branch: values.branch,
      degree: values.degree || undefined,
      cgpa: values.cgpa,
      activeBacklogs: values.activeBacklogs,
      totalBacklogs: values.totalBacklogs,
      passingYear: values.passingYear,
      location: values.location || undefined,
      skills: values.skills
        ? values.skills.split(",").map((s) => s.trim()).filter(Boolean)
        : undefined,
    };
    try {
      await registerStudent.mutateAsync(payload);
      toast.success("Account created. Check your email to verify your address.");
      navigate(paths.login, { replace: true });
    } catch (err) {
      const handled = applyServerViolations(err, setError);
      if (!handled) toast.error(toApiError(err).message || "Registration failed");
    }
  };

  return (
    <AuthLayout
      title="Create your student account"
      subtitle="Register to discover jobs you're eligible for."
      wide
    >
      <form className="space-y-6" onSubmit={handleSubmit(onSubmit)} noValidate>
        <section className="space-y-4">
          <h2 className="text-label-md font-label-md font-semibold text-on-surface-variant uppercase tracking-wide">
            Account
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Full name" leadingIcon="person" error={errors.fullName?.message} {...register("fullName")} />
            <Input label="Email address" leadingIcon="mail" type="email" autoComplete="email" error={errors.email?.message} {...register("email")} />
            <Input label="Phone (optional)" leadingIcon="call" error={errors.phone?.message} {...register("phone")} />
            <div className="hidden sm:block" />
            <Input label="Password" leadingIcon="lock" type="password" autoComplete="new-password" error={errors.password?.message} {...register("password")} />
            <Input label="Confirm password" leadingIcon="lock" type="password" autoComplete="new-password" error={errors.confirmPassword?.message} {...register("confirmPassword")} />
          </div>
        </section>

        <section className="space-y-4">
          <h2 className="text-label-md font-label-md font-semibold text-on-surface-variant uppercase tracking-wide">
            Academic profile
          </h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="University domain"
              leadingIcon="school"
              placeholder="e.g. nitk.edu.in"
              hint="Your university email or its domain, ending in .edu.in."
              error={errors.universityDomain?.message}
              {...register("universityDomain")}
            />
            <Input label="Roll number" leadingIcon="badge" error={errors.rollNumber?.message} {...register("rollNumber")} />
            <Input label="Department" error={errors.department?.message} {...register("department")} />
            <Input label="Branch" error={errors.branch?.message} {...register("branch")} />
            <Input label="Degree (optional)" error={errors.degree?.message} {...register("degree")} />
            <Input label="Passing year" type="number" error={errors.passingYear?.message} {...register("passingYear")} />
            <Input label="CGPA" type="number" step="0.01" error={errors.cgpa?.message} {...register("cgpa")} />
            <Input label="Location (optional)" leadingIcon="location_on" error={errors.location?.message} {...register("location")} />
            <Input label="Active backlogs" type="number" error={errors.activeBacklogs?.message} {...register("activeBacklogs")} />
            <Input label="Total backlogs" type="number" error={errors.totalBacklogs?.message} {...register("totalBacklogs")} />
          </div>
          <Input
            label="Skills (optional)"
            leadingIcon="code"
            placeholder="Java, React, SQL"
            hint="Comma-separated."
            error={errors.skills?.message}
            {...register("skills")}
          />
        </section>

        <Button type="submit" className="w-full" size="lg" isLoading={isSubmitting || registerStudent.isPending}>
          Create account
        </Button>
      </form>

      <div className="mt-6 text-center text-body-md font-body-md text-on-surface-variant">
        Already have an account?{" "}
        <Link to={paths.login} className="font-medium text-primary hover:text-on-primary-fixed-variant">
          Sign in
        </Link>
      </div>
    </AuthLayout>
  );
}
