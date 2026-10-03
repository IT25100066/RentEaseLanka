import API from './axiosInstance';

// ── Admin endpoints (ADMIN role) ──
export const createManager = async (data) => {
  const response = await API.post('/admin/managers', data);
  return response.data;
};

// ── Manager endpoints (MANAGER role) ──
export const createStaff = async (data) => {
  const response = await API.post('/manager/staff', data);
  return response.data;
};


// ── Staff endpoints (STAFF role) ──
export const getPendingCustomers = async () => {
  const response = await API.get('/staff/pending-customers');
  return response.data;
};

export const verifyCustomer = async (id) => {
  const response = await API.post(`/staff/verify-customer/${id}`);
  return response.data;
};

export const rejectCustomer = async (id) => {
  const response = await API.post(`/staff/reject-customer/${id}`);
  return response.data;
};

export const getNicFileBlob = async (fileName) => {
  const response = await API.get(`/staff/file/${encodeURIComponent(fileName)}`, {
    responseType: 'blob', // stream raw binary; caller creates an Object URL
  });
  return response.data; // Blob
};

// ── Customer endpoints (CUSTOMER role) ──
export const uploadNic = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await API.post('/customer/nic-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};
