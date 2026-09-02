import apiClient from './apiClient.js';

export async function getBudgets(params) {
  const { data } = await apiClient.get('/budgets', { params });
  return data.data;
}

export async function createBudget(payload) {
  const { data } = await apiClient.post('/budgets', payload);
  return data.data;
}

export async function updateBudget(id, payload) {
  const { data } = await apiClient.put(`/budgets/${id}`, payload);
  return data.data;
}

export async function deleteBudget(id) {
  await apiClient.delete(`/budgets/${id}`);
}
