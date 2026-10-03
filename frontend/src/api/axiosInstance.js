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

// ──────────────────────────────────────────────
// Module-level token (in-memory only, never persisted)
// ──────────────────────────────────────────────
let _token = null;

/**
 * Called by AuthContext to set/clear the token in the axios module.
 */
export function setAxiosToken(token) {
  _token = token;
}

/**
 * Robustly extracts human-readable, meaningful error messages from backend error responses
 */
export function extractErrorMessage(error, fallback = 'An unexpected error occurred') {
  if (!error) return fallback;
  if (typeof error === 'string') return error;

  if (error.userMessage && typeof error.userMessage === 'string') {
    return error.userMessage;
  }

  if (error.response?.data) {
    const data = error.response.data;
    if (typeof data === 'string') return data;
    if (data.message && typeof data.message === 'string') return data.message;
    if (Array.isArray(data.details) && data.details.length > 0) return data.details.join('; ');
    if (data.error && typeof data.error === 'string') return data.error;
    if (Array.isArray(data.errors) && data.errors.length > 0) {
      return data.errors.map(e => e.defaultMessage || e.message || String(e)).join('; ');
    }
  }

  if (error.message && typeof error.message === 'string') return error.message;
  return fallback;
}

// Request interceptor — injects JWT from in-memory store
API.interceptors.request.use(
  (config) => {
    if (_token) {
      config.headers.Authorization = `Bearer ${_token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor — global error handling
API.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = extractErrorMessage(error);
    error.userMessage = message;
    console.error('[API Error]', message, error);

    // On 401, clear the in-memory token (session expired / invalid)
    if (error.response?.status === 401) {
      _token = null;
    }

    return Promise.reject(error);
  }
);

export default API;
