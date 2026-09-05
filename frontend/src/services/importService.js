import apiClient from './apiClient.js';

export async function previewImport(file) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await apiClient.post('/imports/preview', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data;
}
