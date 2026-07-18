import { AxiosError } from "axios";
import type { ApiResponse } from "@/types/api";

/**
 * Normalised error thrown by the service layer. Carries the backend's stable error code and
 * any field-level violations so forms and toasts can render precise messages.
 */
export class ApiRequestError extends Error {
  readonly code?: string;
  readonly status?: number;
  readonly violations?: { field: string; message: string }[];

  constructor(message: string, opts: { code?: string; status?: number; violations?: { field: string; message: string }[] } = {}) {
    super(message);
    this.name = "ApiRequestError";
    this.code = opts.code;
    this.status = opts.status;
    this.violations = opts.violations;
  }
}

/** Unwraps `ApiResponse<T>.data`, throwing an ApiRequestError if the envelope reports failure. */
export function unwrap<T>(response: ApiResponse<T>): T {
  if (!response.success || response.data === undefined || response.data === null) {
    throw new ApiRequestError(response.message || "Request failed", {
      code: response.error?.code,
      status: response.error?.status,
      violations: response.error?.violations?.map((v) => ({ field: v.field, message: v.message })),
    });
  }
  return response.data;
}

/** Converts any thrown value (axios or otherwise) into a consistent ApiRequestError. */
export function toApiError(error: unknown): ApiRequestError {
  if (error instanceof ApiRequestError) return error;
  if (error instanceof AxiosError) {
    const body = error.response?.data as ApiResponse<unknown> | undefined;
    return new ApiRequestError(body?.message || error.message || "Network error", {
      code: body?.error?.code,
      status: body?.error?.status ?? error.response?.status,
      violations: body?.error?.violations?.map((v) => ({ field: v.field, message: v.message })),
    });
  }
  if (error instanceof Error) return new ApiRequestError(error.message);
  return new ApiRequestError("Unexpected error");
}

// Loosely typed to accept react-hook-form's UseFormSetError<T> for any T. The field name comes
// from the backend as a plain string; we cast it to the form's Path union at the call boundary.
type FieldSetter<TName extends string> = (
  name: TName,
  error: { type: string; message: string },
) => void;

/**
 * Maps backend field-level validation violations onto a react-hook-form `setError`. Returns true
 * when at least one violation was applied, so the caller can skip a generic toast.
 */
export function applyServerViolations<TName extends string>(
  error: unknown,
  setError: FieldSetter<TName>,
): boolean {
  const apiError = toApiError(error);
  if (!apiError.violations?.length) return false;
  let applied = false;
  for (const v of apiError.violations) {
    if (v.field) {
      setError(v.field as TName, { type: "server", message: v.message });
      applied = true;
    }
  }
  return applied;
}
