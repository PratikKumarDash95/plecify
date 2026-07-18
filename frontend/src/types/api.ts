// Wire types mirroring the backend's uniform response envelope
// (com.campusconnect.portal.common.response.*). Every REST call returns ApiResponse<T>;
// list endpoints put a PagedResponse<T> in the `data` field.

export interface FieldViolation {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export interface ApiError {
  code: string;
  status: number;
  violations?: FieldViolation[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: ApiError;
  timestamp: string;
  path?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

/** Spring Data pageable query parameters accepted by list endpoints. */
export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}
