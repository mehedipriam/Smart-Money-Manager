import apiClient from './apiClient.js';

export async function getGoals(status) {
  const { data } = await apiClient.get('/goals', { params: status ? { status } : undefined });
  return data.data;
}

export async function createGoal(payload) {
  const { data } = await apiClient.post('/goals', payload);
  return data.data;
}

export async function updateGoal(id, payload) {
  const { data } = await apiClient.put(`/goals/${id}`, payload);
  return data.data;
}

export async function deleteGoal(id) {
  await apiClient.delete(`/goals/${id}`);
}

export async function addContribution(id, payload) {
  const { data } = await apiClient.post(`/goals/${id}/contributions`, payload);
  return data.data;
}

export async function getContributions(id) {
  const { data } = await apiClient.get(`/goals/${id}/contributions`);
  return data.data;
}
