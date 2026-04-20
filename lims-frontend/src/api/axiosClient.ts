import axios from 'axios';

// Singleton Axios instance to be used across the entire application
export const apiClient = axios.create({
  baseURL: '/api', // Vite proxy catches this and routes to localhost:8080/api
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Automatically inject the JWT token into all outgoing requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('lims_jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle global 401/403 (Token expired/invalid)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      // Clear compromised/expired token and force a hard navigation to login
      localStorage.removeItem('lims_jwt_token');
      // Using window.location to circumvent React Router context if deep in the tree
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
