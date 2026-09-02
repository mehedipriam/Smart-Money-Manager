import apiClient from './apiClient.js';

export async function getDashboard(params) {
  const { data } = await apiClient.get('/dashboard', { params });
  return data.data;
}
