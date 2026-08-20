import api from "./api";

export type PromotionStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface PromotionRequest {
  id: string;
  userId: string;
  username: string;
  justification: string;
  status: PromotionStatus;
  reviewedByUsername: string | null;
  createdAt: string;
  reviewedAt: string | null;
}

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  role: string;
  status: string;
}

export const submitPromotionRequest = (justification: string) =>
  api.post<PromotionRequest>("/api/promotion-requests", { justification }).then((r) => r.data);

export const getMyPromotionRequests = () =>
  api.get<PromotionRequest[]>("/api/promotion-requests/mine").then((r) => r.data);

export const getPendingPromotionRequests = () =>
  api.get<PromotionRequest[]>("/api/admin/promotion-requests").then((r) => r.data);

export const getAllPromotionRequests = () =>
  api.get<PromotionRequest[]>("/api/admin/promotion-requests/all").then((r) => r.data);

export const approvePromotionRequest = (id: string) =>
  api.post<PromotionRequest>(`/api/admin/promotion-requests/${id}/approve`).then((r) => r.data);

export const rejectPromotionRequest = (id: string) =>
  api.post<PromotionRequest>(`/api/admin/promotion-requests/${id}/reject`).then((r) => r.data);

export const getAllUsers = () =>
  api.get<AdminUser[]>("/api/admin/users").then((r) => r.data);

export const setUserRole = (userId: string, role: string) =>
  api.patch(`/api/admin/users/${userId}/role`, { role });
