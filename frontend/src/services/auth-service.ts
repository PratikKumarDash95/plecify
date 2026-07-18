import { apiClient } from "@/lib/api-client";
import { unwrap } from "@/lib/api-helpers";
import type { ApiResponse } from "@/types/api";
import type {
  AuthResponse,
  ForgotPasswordRequest,
  GoogleLoginRequest,
  LoginChallengeResponse,
  LoginRequest,
  LoginResponse,
  RegisterCompanyRequest,
  RegisterStudentRequest,
  ResendOtpRequest,
  ResendVerificationRequest,
  ResetPasswordRequest,
  VerifyOtpRequest,
} from "@/types/auth";
import { tokenStorage } from "@/lib/token-storage";

/** Thin, typed wrapper over the /api/v1/auth endpoints. Unwraps the ApiResponse envelope. */
export const authService = {
  /** Step 1: verify credentials. On success an OTP is emailed; no tokens are returned yet. */
  async login(payload: LoginRequest): Promise<LoginChallengeResponse> {
    const { data } = await apiClient.post<ApiResponse<LoginChallengeResponse>>("/auth/login", payload);
    return unwrap(data);
  },

  /** Step 2: exchange the emailed OTP for an access/refresh token pair. */
  async verifyOtp(payload: VerifyOtpRequest): Promise<LoginResponse> {
    const { data } = await apiClient.post<ApiResponse<LoginResponse>>("/auth/verify-otp", payload);
    return unwrap(data);
  },

  /** Re-sends the login OTP for an in-progress two-step login. */
  async resendOtp(payload: ResendOtpRequest): Promise<string> {
    const { data } = await apiClient.post<ApiResponse<void>>("/auth/resend-otp", payload);
    return data.message;
  },

  /** Exchanges a Google ID token for our own access/refresh token pair. */
  async googleLogin(payload: GoogleLoginRequest): Promise<LoginResponse> {
    const { data } = await apiClient.post<ApiResponse<LoginResponse>>("/auth/google", payload);
    return unwrap(data);
  },

  async registerStudent(payload: RegisterStudentRequest): Promise<AuthResponse> {
    const { data } = await apiClient.post<ApiResponse<AuthResponse>>("/auth/register/student", payload);
    return unwrap(data);
  },

  async registerCompany(payload: RegisterCompanyRequest): Promise<AuthResponse> {
    const { data } = await apiClient.post<ApiResponse<AuthResponse>>("/auth/register/company", payload);
    return unwrap(data);
  },

  async forgotPassword(payload: ForgotPasswordRequest): Promise<string> {
    const { data } = await apiClient.post<ApiResponse<void>>("/auth/forgot-password", payload);
    return data.message;
  },

  async resetPassword(payload: ResetPasswordRequest): Promise<string> {
    const { data } = await apiClient.post<ApiResponse<void>>("/auth/reset-password", payload);
    return data.message;
  },

  async resendVerification(payload: ResendVerificationRequest): Promise<string> {
    const { data } = await apiClient.post<ApiResponse<void>>("/auth/resend-verification", payload);
    return data.message;
  },

  async verifyEmail(token: string): Promise<string> {
    const { data } = await apiClient.get<ApiResponse<{ message: string }>>("/auth/verify-email", {
      params: { token },
    });
    return data.data?.message ?? data.message;
  },

  /** Logs out server-side (revokes the refresh token) then clears local tokens. Best-effort. */
  async logout(): Promise<void> {
    const refreshToken = tokenStorage.getRefreshToken();
    try {
      if (refreshToken) {
        await apiClient.post<ApiResponse<void>>("/auth/logout", { refreshToken });
      }
    } finally {
      tokenStorage.clear();
    }
  },
};
