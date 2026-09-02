import apiClient from './apiClient.js';

export async function getBills(status) {
  const { data } = await apiClient.get('/bills', { params: status ? { status } : undefined });
  return data.data;
}

export async function getUpcomingBills(limit = 5) {
  const { data } = await apiClient.get('/bills/upcoming', { params: { limit } });
  return data.data;
}

export async function createBill(payload) {
  const { data } = await apiClient.post('/bills', payload);
  return data.data;
}

export async function updateBill(id, payload) {
  const { data } = await apiClient.put(`/bills/${id}`, payload);
  return data.data;
}

export async function deleteBill(id) {
  await apiClient.delete(`/bills/${id}`);
}

export async function markAsPaid(id) {
  const { data } = await apiClient.post(`/bills/${id}/pay`);
  return data.data;
}
