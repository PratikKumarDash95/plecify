import { useMutation } from "@tanstack/react-query";
import { authService } from "@/services/auth-service";
import type {
  ForgotPasswordRequest,
  RegisterCompanyRequest,
  RegisterStudentRequest,
  ResendVerificationRequest,
  ResetPasswordRequest,
} from "@/types/auth";

export function useRegisterStudent() {
  return useMutation({
    mutationFn: (payload: RegisterStudentRequest) => authService.registerStudent(payload),
  });
}

export function useRegisterCompany() {
  return useMutation({
    mutationFn: (payload: RegisterCompanyRequest) => authService.registerCompany(payload),
  });
}

export function useForgotPassword() {
  return useMutation({
    mutationFn: (payload: ForgotPasswordRequest) => authService.forgotPassword(payload),
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (payload: ResetPasswordRequest) => authService.resetPassword(payload),
  });
}

export function useResendVerification() {
  return useMutation({
    mutationFn: (payload: ResendVerificationRequest) => authService.resendVerification(payload),
  });
}

export function useVerifyEmail() {
  return useMutation({
    mutationFn: (token: string) => authService.verifyEmail(token),
  });
}
