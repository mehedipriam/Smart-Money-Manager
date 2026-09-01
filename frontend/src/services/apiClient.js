import axios from 'axios';
import { clearSession, getAccessToken, getRefreshToken, setSession, getStoredUser } from '../utils/tokenStorage.js';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let pendingRequests = [];

function resolvePending(newToken) {
  pendingRequests.forEach(({ resolve }) => resolve(newToken));
  pendingRequests = [];
}

function rejectPending(error) {
  pendingRequests.forEach(({ reject }) => reject(error));
  pendingRequests = [];
}

/** Any request whose 401 must NOT trigger a refresh attempt (it's already an auth flow). */
function isAuthEndpoint(url = '') {
  return url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/register');
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      return Promise.reject(error);
    }

    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      clearSession();
      window.dispatchEvent(new Event('smm:auth-expired'));
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingRequests.push({ resolve, reject });
      }).then((newToken) => {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const { data } = await apiClient.post('/auth/refresh', { refreshToken });
      const { accessToken, refreshToken: newRefreshToken } = data.data;
      setSession({ accessToken, refreshToken: newRefreshToken, user: getStoredUser() });
      resolvePending(accessToken);
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      clearSession();
      rejectPending(refreshError);
      window.dispatchEvent(new Event('smm:auth-expired'));
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default apiClient;
