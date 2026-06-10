const ACCESS_TOKEN_KEY = 'lifequest.accessToken';
const REFRESH_TOKEN_KEY = 'lifequest.refreshToken';

export interface AuthTokenPair {
  accessToken: string;
  refreshToken: string;
}

const storage = () => window.localStorage;

export function getAccessToken(): string | null {
  return storage().getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return storage().getItem(REFRESH_TOKEN_KEY);
}

export function setAuthTokens(tokens: AuthTokenPair): void {
  storage().setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  storage().setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearAuthTokens(): void {
  storage().removeItem(ACCESS_TOKEN_KEY);
  storage().removeItem(REFRESH_TOKEN_KEY);
}

export function hasAccessToken(): boolean {
  return Boolean(getAccessToken());
}

