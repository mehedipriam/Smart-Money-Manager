import apiClient from './apiClient.js';

export async function register({ fullName, email, password, phone }) {
  const { data } = await apiClient.post('/auth/register', { fullName, email, password, phone });
  return data.data;
}

export async function login({ email, password }) {
  const { data } = await apiClient.post('/auth/login', { email, password });
  return data.data;
}

export async function logout() {
  await apiClient.post('/auth/logout');
}

export async function verifyEmail(token) {
  const { data } = await apiClient.get('/auth/verify-email', { params: { token } });
  return data.message;
}

export async function resendVerification(email) {
  const { data } = await apiClient.post('/auth/resend-verification', { email });
  return data.message;
}

export async function forgotPassword(email) {
  const { data } = await apiClient.post('/auth/forgot-password', { email });
  return data.message;
}

export async function resetPassword({ token, newPassword }) {
  const { data } = await apiClient.post('/auth/reset-password', { token, newPassword });
  return data.message;
}
