import { request } from '@/api/http';
import type {
  AuthTokenResponse,
  CurrentUser,
  LoginRequest,
  RefreshTokenRequest,
  RegisterRequest,
} from '@/types';

export function login(payload: LoginRequest): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>({
    method: 'POST',
    url: '/auth/login',
    data: payload,
  });
}

export function register(payload: RegisterRequest): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>({
    method: 'POST',
    url: '/auth/register',
    data: payload,
  });
}

export function refreshToken(payload: RefreshTokenRequest): Promise<AuthTokenResponse> {
  return request<AuthTokenResponse>({
    method: 'POST',
    url: '/auth/refresh',
    data: payload,
  });
}

export function logout(): Promise<null> {
  return request<null>({
    method: 'POST',
    url: '/auth/logout',
  });
}

export function getCurrentUser(): Promise<CurrentUser> {
  return request<CurrentUser>({
    method: 'GET',
    url: '/users/me',
  });
}

