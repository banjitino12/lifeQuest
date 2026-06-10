import axios, { type AxiosError, type AxiosRequestConfig } from 'axios';
import router from '@/router';
import { ApiError, type ApiResponse } from '@/types';
import { clearAuthTokens, getAccessToken } from '@/utils/authToken';

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

http.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status;
    const payload = error.response?.data;

    if (status === 401) {
      clearAuthTokens();
      if (router.currentRoute.value.name !== 'login') {
        await router.replace({
          name: 'login',
          query: { redirect: router.currentRoute.value.fullPath },
        });
      }
    }

    throw new ApiError({
      code: payload?.code ?? 'INTERNAL_ERROR',
      message: payload?.message ?? error.message,
      status,
      data: payload?.data,
    });
  },
);

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await http.request<ApiResponse<T>>(config);
  return response.data.data;
}

export default http;

