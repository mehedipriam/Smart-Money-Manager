import apiClient from './apiClient.js';

export async function getNotifications(unreadOnly = false) {
  const { data } = await apiClient.get('/notifications', { params: { unreadOnly } });
  return data.data;
}

export async function getUnreadCount() {
  const { data } = await apiClient.get('/notifications/unread-count');
  return data.data.count;
}

export async function markAsRead(id) {
  const { data } = await apiClient.put(`/notifications/${id}/read`);
  return data.data;
}

export async function markAllAsRead() {
  await apiClient.put('/notifications/read-all');
}

export async function deleteNotification(id) {
  await apiClient.delete(`/notifications/${id}`);
}
