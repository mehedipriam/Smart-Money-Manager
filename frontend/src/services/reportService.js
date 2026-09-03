import apiClient from './apiClient.js';

export async function getReportSummary(params) {
  const { data } = await apiClient.get('/reports/summary', { params });
  return data.data;
}

async function downloadFile(path, params, filename) {
  const response = await apiClient.get(path, { params, responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export function exportCsv(params) {
  return downloadFile('/reports/export/csv', params, 'financial-report.csv');
}

export function exportPdf(params) {
  return downloadFile('/reports/export/pdf', params, 'financial-report.pdf');
}
