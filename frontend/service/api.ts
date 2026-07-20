import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const stored = localStorage.getItem("auth");
    if (stored) {
      const { accessToken } = JSON.parse(stored);
      if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
    }
  }
  return config;
});

export default api;
