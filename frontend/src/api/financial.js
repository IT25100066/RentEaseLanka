import API from './axiosInstance';

export const getIncomeSummary = async ({ from, to, status }) => {
  const response = await API.get('/financial/income', {
    params: { from, to, status },
  });
  return response.data;
};
