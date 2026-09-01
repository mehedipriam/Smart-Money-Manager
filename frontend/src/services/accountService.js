import apiClient from './apiClient.js';

export async function getAccounts() {
  const { data } = await apiClient.get('/accounts');
  return data.data;
}

export async function getAccount(id) {
  const { data } = await apiClient.get(`/accounts/${id}`);
  return data.data;
}

export async function createAccount(payload) {
  const { data } = await apiClient.post('/accounts', payload);
  return data.data;
}

export async function updateAccount(id, payload) {
  const { data } = await apiClient.put(`/accounts/${id}`, payload);
  return data.data;
}

export async function deleteAccount(id) {
  await apiClient.delete(`/accounts/${id}`);
}

export async function transfer(payload) {
  const { data } = await apiClient.post('/accounts/transfer', payload);
  return data.data;
}
