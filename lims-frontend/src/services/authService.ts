import { apiClient } from '../api/axiosClient';

export interface LoginResponse {
  token: string;
}

export const login = async (username: string, password: string):Promise<void> => {
  // Assuming the backend has a /api/auth/login endpoint 
  // We send the JSON payload to receive the JWT Token
  const response = await apiClient.post<LoginResponse>('/auth/login', {
    username,
    password
  });
  
  if (response.data.token) {
    localStorage.setItem('lims_jwt_token', response.data.token);
  } else {
    throw new Error('No token received from backend');
  }
};

export const logout = () => {
  localStorage.removeItem('lims_jwt_token');
  window.location.href = '/login';
};

export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem('lims_jwt_token');
};
