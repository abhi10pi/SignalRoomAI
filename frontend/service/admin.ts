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

// Moderation
export interface Report {
  id: string;
  reporter: { id: string; username: string };
  signalId?: string;
  opinionId?: string;
  commentId?: string;
  reason: string;
  detail?: string;
  status: string;
  createdAt: string;
}

export const getPendingReports = () =>
  api.get<Report[]>("/api/admin/reports").then((r) => r.data);

export const reviewReport = (id: string, action: "action" | "dismiss") =>
  api.post<Report>(`/api/admin/reports/${id}/review`, { action }).then((r) => r.data);

export const hideOpinion = (id: string) => api.post(`/api/admin/opinions/${id}/hide`);
export const restoreOpinion = (id: string) => api.post(`/api/admin/opinions/${id}/restore`);
export const hideComment = (id: string) => api.post(`/api/admin/comments/${id}/hide`);
export const restoreComment = (id: string) => api.post(`/api/admin/comments/${id}/restore`);
export const retryAi = (signalId: string) => api.post(`/api/admin/signals/${signalId}/retry-ai`).then((r) => r.data);
