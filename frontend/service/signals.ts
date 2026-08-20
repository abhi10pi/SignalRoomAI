import api from "./api";

export type SignalStatus = "DRAFT" | "PENDING_VALIDATION" | "VALIDATED" | "REJECTED" | "EVALUATED" | "EXPIRED_UNRESOLVED";
export type Visibility = "PUBLIC" | "PRIVATE";
export type ResolutionType = "QUANTITATIVE" | "NEWS_VERIFIABLE" | "SUBJECTIVE";

export interface SignalSummary {
  id: string;
  title: string;
  status: SignalStatus;
  visibility: Visibility;
  domainName: string;
  domainSlug: string;
  submitterUsername: string;
  resolutionDate: string;
  submittedAt: string | null;
  createdAt: string;
}

export interface SignalDetail extends SignalSummary {
  description: string;
  resolutionType: ResolutionType;
  resolutionCriteria: string;
  submitterId: string;
  domainId: string;
  updatedAt: string;
    actualOutcome: Outcome | null;
}

export type Outcome = "TRUE" | "FALSE" | "AMBIGUOUS";
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CreateSignalPayload {
  title: string;
  description: string;
  domainId: string;
  resolutionType: ResolutionType;
  resolutionCriteria: string;
  resolutionDate: string;
  visibility: Visibility;
}

export type UpdateSignalPayload = Partial<CreateSignalPayload>;

export const createSignal = (data: CreateSignalPayload) =>
  api.post<SignalDetail>("/api/signals", data).then((r) => r.data);

export const updateSignal = (id: string, data: UpdateSignalPayload) =>
  api.put<SignalDetail>(`/api/signals/${id}`, data).then((r) => r.data);

export const deleteSignal = (id: string) =>
  api.delete(`/api/signals/${id}`);

export const publishSignal = (id: string) =>
  api.post<SignalDetail>(`/api/signals/${id}/publish`).then((r) => r.data);

export const getSignal = (id: string) =>
  api.get<SignalDetail>(`/api/signals/${id}`).then((r) => r.data);

export const getMySignals = () =>
  api.get<SignalSummary[]>("/api/users/me/signals").then((r) => r.data);

export const getPublicFeed = (page = 0, size = 20, sort?: string) =>
  api.get<PageResponse<SignalSummary>>("/api/signals", { params: { page, size, sort } }).then((r) => r.data);

export const getDomainFeed = (slug: string, page = 0, size = 20) =>
  api.get<PageResponse<SignalSummary>>(`/api/signals/domain/${slug}`, { params: { page, size } }).then((r) => r.data);

export const searchSignals = (q: string, page = 0, size = 20) =>
  api.get<PageResponse<SignalSummary>>("/api/signals/search", { params: { q, page, size } }).then((r) => r.data);
