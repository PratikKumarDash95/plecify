// Auth domain types mirroring com.campusconnect.portal.dto.auth.*
// Roles are carried WITHOUT the ROLE_ prefix (e.g. "STUDENT"), matching the backend.

export type Role = "STUDENT" | "COMPANY" | "PLACEMENT_CELL" | "ADMIN";

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  emailVerified: boolean;
  enabled: boolean;
  roles: Role[];
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  role: Role;
  user: UserResponse;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

/** Step-one login result: credentials accepted, an OTP was emailed, no tokens yet. */
export interface LoginChallengeResponse {
  email: string;
  otpRequired: boolean;
  /** OTP lifetime in seconds. */
  expiresIn: number;
}

export interface AuthResponse {
  user: UserResponse;
  message: string;
}

// --- request payloads ---------------------------------------------------------

export interface LoginRequest {
  email: string;
  password: string;
}

export interface VerifyOtpRequest {
  email: string;
  code: string;
}

export interface ResendOtpRequest {
  email: string;
}

export interface GoogleLoginRequest {
  /** Google-issued ID token (the `credential` from Google Identity Services). */
  idToken: string;
}

export type WorkAuthorization = "CITIZEN" | "PERMANENT_RESIDENT" | "REQUIRES_SPONSORSHIP" | "ANY";

export interface RegisterStudentRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
  universityDomain: string;
  rollNumber: string;
  department: string;
  branch: string;
  degree?: string;
  cgpa: number;
  activeBacklogs: number;
  totalBacklogs: number;
  passingYear: number;
  location?: string;
  skills?: string[];
}

export interface RegisterCompanyRequest {
  email: string;
  password: string;
  contactPersonName: string;
  phone?: string;
  companyName: string;
  industry?: string;
  website?: string;
  description?: string;
  headquarters?: string;
  contactEmail?: string;
  contactPhone?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ResendVerificationRequest {
  email: string;
}
