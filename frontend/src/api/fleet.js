import API from './axiosInstance';

export const getVehicles = () => API.get('/vehicles');

export const getVehicleById = (id) => API.get(`/vehicle/${id}`);

export const addVehicleImages = (vehicleId, files) => {
  const formData = new FormData();
  files.forEach((file) => formData.append('images', file));
  return API.post(`/vehicle/${vehicleId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
