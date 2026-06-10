export type ApiCode =
  | 'OK'
  | 'CREATED'
  | 'VALIDATION_ERROR'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'CONFLICT'
  | 'RATE_LIMITED'
  | 'LLM_UNAVAILABLE'
  | 'INTERNAL_ERROR'
  | string;

export interface ApiResponse<T> {
  code: ApiCode;
  message: string;
  data: T;
}

export interface ApiErrorPayload {
  code: ApiCode;
  message: string;
  status?: number;
  data?: unknown;
}

export class ApiError extends Error {
  code: ApiCode;
  status?: number;
  data?: unknown;

  constructor(payload: ApiErrorPayload) {
    super(payload.message);
    this.name = 'ApiError';
    this.code = payload.code;
    this.status = payload.status;
    this.data = payload.data;
  }
}

