import API from './axiosInstance';

export async function getPublicReviews() {
  const res = await API.get('/reviews/public');
  return res.data;
}

export async function getMyReviews() {
  const res = await API.get('/reviews/my');
  return res.data;
}

export async function submitReview(reviewData) {
  const res = await API.post('/reviews', reviewData);
  return res.data;
}

export async function deleteReview(id) {
  const res = await API.delete(`/reviews/${id}`);
  return res.data;
}
