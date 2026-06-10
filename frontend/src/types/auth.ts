export interface AuthTokenResponse {
  userId: number;
  username: string;
  accessToken: string;
  refreshToken: string;
  profileCompleted: boolean;
}

export interface LoginRequest {
  account: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email?: string;
  phone?: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface CurrentUser {
  userId: number;
  username: string;
  email?: string | null;
  phone?: string | null;
  avatarUrl?: string | null;
  profileCompleted?: boolean;
}

