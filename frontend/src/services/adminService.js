import apiClient from './apiClient.js';

export async function getStats() {
  const { data } = await apiClient.get('/admin/stats');
  return data.data;
}

export async function getUsers(params) {
  const { data } = await apiClient.get('/admin/users', { params });
  return data.data;
}

export async function getUser(id) {
  const { data } = await apiClient.get(`/admin/users/${id}`);
  return data.data;
}

export async function enableUser(id) {
  const { data } = await apiClient.put(`/admin/users/${id}/enable`);
  return data.data;
}

export async function disableUser(id) {
  const { data } = await apiClient.put(`/admin/users/${id}/disable`);
  return data.data;
}
