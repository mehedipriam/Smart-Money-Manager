import apiClient from './apiClient.js';

export async function getTransactions(params) {
  const { data } = await apiClient.get('/transactions', { params });
  return data.data;
}

export async function createTransaction(payload) {
  const { data } = await apiClient.post('/transactions', payload);
  return data.data;
}

export async function updateTransaction(id, payload) {
  const { data } = await apiClient.put(`/transactions/${id}`, payload);
  return data.data;
}

export async function deleteTransaction(id) {
  await apiClient.delete(`/transactions/${id}`);
}
