import apiClient from './apiClient.js';

export async function getProfile() {
  const { data } = await apiClient.get('/users/me');
  return data.data;
}

export async function updateProfile(payload) {
  const { data } = await apiClient.put('/users/me', payload);
  return data.data;
}

export async function changePassword({ currentPassword, newPassword }) {
  const { data } = await apiClient.put('/users/me/password', { currentPassword, newPassword });
  return data.message;
}
