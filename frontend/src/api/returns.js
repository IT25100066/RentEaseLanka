import API from './axiosInstance';

export const inspectVehicleReturn = async (bookingId, data) => {
  const response = await API.post(`/returns/booking/${bookingId}/inspect`, data);
  return response.data;
};

export const getReturnInspection = async (bookingId) => {
  const response = await API.get(`/returns/booking/${bookingId}`);
  return response.data;
};
