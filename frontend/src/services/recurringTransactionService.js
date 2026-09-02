import apiClient from './apiClient.js';

export async function getRecurringTransactions() {
  const { data } = await apiClient.get('/recurring-transactions');
  return data.data;
}

export async function createRecurringTransaction(payload) {
  const { data } = await apiClient.post('/recurring-transactions', payload);
  return data.data;
}

export async function updateRecurringTransaction(id, payload) {
  const { data } = await apiClient.put(`/recurring-transactions/${id}`, payload);
  return data.data;
}

export async function deleteRecurringTransaction(id) {
  await apiClient.delete(`/recurring-transactions/${id}`);
}
