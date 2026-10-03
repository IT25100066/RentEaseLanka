import API from './axiosInstance';

// ─── READ ──────────────────────────────────────────────────────────────────

export const getAllHandovers = async (status = null) => {
  const params = status ? { status } : {};
  const res = await API.get('/handovers', { params });
  return res.data;
};

export const getHandoverById = async (id) => {
  const res = await API.get(`/handovers/${id}`);
  return res.data;
};

export const getHandoversByVehicleId = async (vehicleId) => {
  const res = await API.get(`/handovers/vehicle/${vehicleId}`);
  return res.data;
};

// ─── WRITE ─────────────────────────────────────────────────────────────────

export const initiateHandover = async (data) => {
  const res = await API.post('/handovers', data);
  return res.data;
};

export const updateHandover = async (id, data) => {
  const res = await API.put(`/handovers/${id}`, data);
  return res.data;
};

export const deleteHandover = async (id) => {
  const res = await API.delete(`/handovers/${id}`);
  return res.data;
};



export const recordMileage = async (id, data) => {
  const res = await API.put(`/handovers/${id}/record-mileage`, data);
  return res.data;
};

export const recordDamage = async (id, data) => {
  const res = await API.put(`/handovers/${id}/record-damage`, data);
  return res.data;
};

export const completeHandover = async (id) => {
  const res = await API.put(`/handovers/${id}/complete`);
  return res.data;
};

export const cancelHandover = async (id) => {
  const res = await API.put(`/handovers/${id}/cancel`);
  return res.data;
};
