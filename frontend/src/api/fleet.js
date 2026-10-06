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

/**
 * Sets a vehicle to "Out of Service" status.
 * Requires MANAGER or ADMIN role.
 * @param {number} id - Vehicle ID
 */
export const setVehicleToOutOfService = (id) => API.delete(`/vehicle/${id}/remove`);
