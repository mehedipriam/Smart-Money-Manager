import apiClient from './apiClient.js';

export async function getCategories(type) {
  const { data } = await apiClient.get('/categories', { params: type ? { type } : undefined });
  return data.data;
}

export async function createCategory(payload) {
  const { data } = await apiClient.post('/categories', payload);
  return data.data;
}

export async function updateCategory(id, payload) {
  const { data } = await apiClient.put(`/categories/${id}`, payload);
  return data.data;
}

export async function deleteCategory(id) {
  await apiClient.delete(`/categories/${id}`);
}
