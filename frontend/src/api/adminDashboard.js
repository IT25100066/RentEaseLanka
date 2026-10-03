import API from './axiosInstance';

export const getDashboardSummary = async () => {
  const response = await API.get('/admin/dashboard/summary');
  return response.data;
};

export const getVehiclePerformance = async () => {
  const response = await API.get('/admin/dashboard/vehicles');
  return response.data;
};

export const getVehiclePerformanceById = async (id) => {
  const response = await API.get(`/admin/dashboard/vehicles/${id}`);
  return response.data;
};

export const getDashboardNotifications = async () => {
  const response = await API.get('/admin/dashboard/notifications');
  return response.data;
};

export const getMaintenanceReport = async (startDate, endDate) => {
  const response = await API.get('/admin/reports/maintenance', {
    params: {
      ...(startDate ? { startDate } : {}),
      ...(endDate ? { endDate } : {}),
    },
  });
  return response.data;
};
