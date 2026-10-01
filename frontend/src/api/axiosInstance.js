import axios from 'axios';

// BASE_URL is injected by Vite at build time from .env.development / .env.production.
// In production this resolves to your domain (e.g. https://yourdomain.com).
// In development it falls back to http://localhost:8081.
export const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081';

const API = axios.create({
  baseURL: `${BASE_URL}/api`,
  timeout: 15000,
  headers: {
    'Accept': 'application/json',
  },
});



// Response interceptor for global error handling
API.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data ||
      error.message ||
      'An unexpected error occurred';
    console.error('[API Error]', message);
    return Promise.reject(error);
  }
);

export default API;
