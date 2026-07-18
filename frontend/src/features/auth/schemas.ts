import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});
export type LoginFormValues = z.infer<typeof loginSchema>;

export const otpSchema = z.object({
  code: z
    .string()
    .min(1, "Code is required")
    .regex(/^\d{6}$/, "Enter the 6-digit code"),
});
export type OtpFormValues = z.infer<typeof otpSchema>;

// Backend requires a strong password; mirror the common policy client-side for fast feedback.
const passwordSchema = z
  .string()
  .min(8, "At least 8 characters")
  .regex(/[A-Z]/, "One uppercase letter")
  .regex(/[a-z]/, "One lowercase letter")
  .regex(/[0-9]/, "One number");

export const studentRegisterSchema = z
  .object({
    fullName: z.string().min(1, "Full name is required").max(150),
    email: z
      .string()
      .min(1, "Email is required")
      .email("Enter a valid email")
      .regex(/\.edu\.in$/i, "Email must be a valid .edu.in address"),
    phone: z.string().max(20).optional().or(z.literal("")),
    password: passwordSchema,
    confirmPassword: z.string(),
    universityDomain: z
      .string()
      .min(1, "University domain is required")
      .max(120)
      // Accept either the bare domain (centurionuniv.edu.in) or a full email
      // (1120095@centurionuniv.edu.in) — strip the local part before validating.
      .transform((v) => v.trim().split("@").pop() ?? "")
      .refine((v) => /^[a-z0-9.-]+\.edu\.in$/i.test(v), {
        message: "Enter a valid university domain ending in .edu.in",
      }),
    rollNumber: z.string().min(1, "Roll number is required").max(40),
    department: z.string().min(1, "Department is required").max(100),
    branch: z.string().min(1, "Branch is required").max(100),
    degree: z.string().max(60).optional().or(z.literal("")),
    cgpa: z.coerce.number().min(0, "0–10").max(10, "0–10"),
    activeBacklogs: z.coerce.number().int().min(0).max(100),
    totalBacklogs: z.coerce.number().int().min(0).max(100),
    passingYear: z.coerce
      .number()
      .int()
      .min(2000, "Enter a valid year")
      .max(2100, "Enter a valid year"),
    location: z.string().max(120).optional().or(z.literal("")),
    skills: z.string().max(500).optional().or(z.literal("")),
  })
  .refine((v) => v.password === v.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  })
  .refine((v) => v.totalBacklogs >= v.activeBacklogs, {
    message: "Total backlogs cannot be less than active backlogs",
    path: ["totalBacklogs"],
  });
export type StudentRegisterFormValues = z.infer<typeof studentRegisterSchema>;
// Input differs from output because z.coerce.* accepts `unknown` at the form boundary.
export type StudentRegisterFormInput = z.input<typeof studentRegisterSchema>;

export const companyRegisterSchema = z
  .object({
    companyName: z.string().min(1, "Company name is required").max(150),
    contactPersonName: z.string().min(1, "Contact person is required").max(150),
    email: z.string().min(1, "Email is required").email("Enter a valid email"),
    phone: z.string().max(20).optional().or(z.literal("")),
    password: passwordSchema,
    confirmPassword: z.string(),
    industry: z.string().max(120).optional().or(z.literal("")),
    website: z.string().max(255).url("Enter a valid URL").optional().or(z.literal("")),
    headquarters: z.string().max(200).optional().or(z.literal("")),
    description: z.string().max(5000).optional().or(z.literal("")),
  })
  .refine((v) => v.password === v.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
export type CompanyRegisterFormValues = z.infer<typeof companyRegisterSchema>;

export const forgotPasswordSchema = z.object({
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
});
export type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export const resetPasswordSchema = z
  .object({
    newPassword: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((v) => v.newPassword === v.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
export type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;
