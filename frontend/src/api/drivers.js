import API from './axiosInstance';

export const createDriver = async (data) => {
  const response = await API.post('/manager/drivers', data);
  return response.data;
};

export const assignDriver = async (bookingId, driverId) => {
  const response = await API.post(`/booking/${bookingId}/assign-driver`, { driverId });
  return response.data;
};

export const getAvailableDrivers = async (pickupDateTime, returnDateTime, bookingId = null) => {
  const params = { pickupDateTime, returnDateTime };
  if (bookingId) {
    params.bookingId = bookingId;
  }
  const response = await API.get('/booking/available-drivers', { params });
  return response.data;
};

export const getDriverProfile = async () => {
  const response = await API.get('/driver/profile');
  return response.data;
};

export const getDriverMyBookings = async (status = null) => {
  const params = status ? { status } : {};
  const response = await API.get('/driver/my-bookings', { params });
  return response.data;
};

export const getAssignedBookingsForDriver = async (driverId) => {
  const response = await API.get(`/driver/${driverId}/bookings`);
  return response.data;
};
