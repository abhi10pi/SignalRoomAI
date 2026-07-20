import api from "./api";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  username: string;
  email: string;
  role: string;
}

export const loginApi = (data: { email: string; password: string }) =>
  api.post<AuthResponse>("/api/auth/login", data).then((r) => r.data);

export const registerApi = (data: { username: string; email: string; password: string }) =>
  api.post<AuthResponse>("/api/auth/register", data).then((r) => r.data);
