import API from './axiosInstance';

export const createBooking = async (data) => {
  const response = await API.post('/booking/create', data);
  return response.data;
};

export const calculatePrice = async (data) => {
  const response = await API.post('/booking/calculate-price', data);
  return response.data;
};

export const getAllBookings = async () => {
  const response = await API.get('/booking/all');
  return response.data;
};

export const getBookingById = async (id) => {
  const response = await API.get(`/booking/${id}`);
  return response.data;
};

export const cancelBooking = async (id) => {
  const response = await API.post(`/booking/${id}/cancel`);
  return response.data;
};

export const confirmBooking = async (id) => {
  const response = await API.post(`/booking/${id}/confirm`);
  return response.data;
};

export const activateBooking = async (id) => {
  const response = await API.post(`/booking/${id}/activate`);
  return response.data;
};

export const completeBooking = async (id) => {
  const response = await API.post(`/booking/${id}/complete`);
  return response.data;
};

export const getCustomerBookings = async () => {
  const response = await API.get('/booking/customer');
  return response.data;
};

export const getBookingsByCustomerId = async (customerId) => {
  const response = await API.get(`/booking/customer/${customerId}`);
  return response.data;
};
