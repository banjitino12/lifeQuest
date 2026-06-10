import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

import { authApi } from '@/api';
import type { AuthTokenResponse, CurrentUser, LoginRequest, RegisterRequest } from '@/types';
import { clearAuthTokens, getAccessToken, getRefreshToken, setAuthTokens } from '@/utils/authToken';

function toCurrentUser(tokenResponse: AuthTokenResponse): CurrentUser {
  return {
    userId: tokenResponse.userId,
    username: tokenResponse.username,
    profileCompleted: tokenResponse.profileCompleted,
  };
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(getAccessToken());
  const refreshTokenValue = ref<string | null>(getRefreshToken());
  const currentUser = ref<CurrentUser | null>(null);
  const loading = ref(false);

  const isAuthenticated = computed(() => Boolean(accessToken.value));
  const profileCompleted = computed(() => Boolean(currentUser.value?.profileCompleted));

  function applyAuth(tokenResponse: AuthTokenResponse): void {
    accessToken.value = tokenResponse.accessToken;
    refreshTokenValue.value = tokenResponse.refreshToken;
    currentUser.value = toCurrentUser(tokenResponse);
    setAuthTokens({
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken,
    });
  }

  function clearAuthState(): void {
    accessToken.value = null;
    refreshTokenValue.value = null;
    currentUser.value = null;
    clearAuthTokens();
  }

  async function login(payload: LoginRequest): Promise<AuthTokenResponse> {
    loading.value = true;
    try {
      const response = await authApi.login(payload);
      applyAuth(response);
      return response;
    } finally {
      loading.value = false;
    }
  }

  async function register(payload: RegisterRequest): Promise<AuthTokenResponse> {
    loading.value = true;
    try {
      const response = await authApi.register(payload);
      applyAuth(response);
      return response;
    } finally {
      loading.value = false;
    }
  }

  async function refreshSession(): Promise<AuthTokenResponse | null> {
    if (!refreshTokenValue.value) {
      clearAuthState();
      return null;
    }

    const response = await authApi.refreshToken({ refreshToken: refreshTokenValue.value });
    applyAuth(response);
    return response;
  }

  async function fetchCurrentUser(): Promise<CurrentUser | null> {
    if (!accessToken.value) {
      currentUser.value = null;
      return null;
    }

    currentUser.value = await authApi.getCurrentUser();
    return currentUser.value;
  }

  async function logout(): Promise<void> {
    try {
      if (accessToken.value) {
        await authApi.logout();
      }
    } finally {
      clearAuthState();
    }
  }

  return {
    accessToken,
    currentUser,
    isAuthenticated,
    loading,
    profileCompleted,
    refreshToken: refreshTokenValue,
    clearAuthState,
    fetchCurrentUser,
    login,
    logout,
    refreshSession,
    register,
  };
});

