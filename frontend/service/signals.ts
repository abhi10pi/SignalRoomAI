import api from "./api";

export type SignalStatus = "DRAFT" | "OPEN" | "PROCESSING" | "CLOSED" | "FAILED" | "ARCHIVED" | "PENDING_VALIDATION" | "VALIDATED" | "REJECTED" | "EVALUATED" | "EXPIRED_UNRESOLVED";
export type Visibility = "PUBLIC" | "PRIVATE";
export type ResolutionType = "QUANTITATIVE" | "NEWS_VERIFIABLE" | "SUBJECTIVE";

export interface SignalSummary {
  id: string;
  title: string;
  status: SignalStatus;
  visibility: Visibility;
  domainName?: string;
  domainSlug?: string;
  submitterUsername?: string;
  resolutionDate: string;
  submittedAt?: string | null;
  createdAt: string;
  authorUsername?: string;
  category?: string;
  discussionEnd?: string;
}

export interface SignalDetail extends SignalSummary {
  description: string;
  resolutionType: ResolutionType;
  resolutionCriteria: string;
  submitterId: string;
  domainId: string;
  updatedAt: string;
  actualOutcome?: Outcome | null;
  authorId?: string;
  authorUsername?: string;
  category?: string;
  tags?: string[];
  sources?: SignalSource[];
  discussionStart?: string;
  discussionEnd?: string;
  upVotes?: number;
  downVotes?: number;
  totalVotes?: number;
  upPercent?: number;
  downPercent?: number;
  myVote?: "UP" | "DOWN";
}

export interface SignalSource { id?: string; url: string; title?: string; description?: string; }
export type OpinionPosition = "AGREE" | "DISAGREE" | "NEUTRAL";
export interface Opinion { id: string; signalId: string; userId: string; username: string; position: OpinionPosition; content: string; createdAt: string; sources: SignalSource[]; upVotes: number; downVotes: number; myVote?: "UP" | "DOWN"; }
export interface Comment { id: string; signalId: string; opinionId?: string; userId: string; username: string; parentCommentId?: string; content: string; createdAt: string; replies: Comment[]; }

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
  category: string;
  tags: string[];
  sources: SignalSource[];
  domainId?: string;
  resolutionType?: ResolutionType;
  resolutionCriteria?: string;
  resolutionDate?: string;
  visibility?: Visibility;
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

export const voteSignal = (id: string, voteType: "UP" | "DOWN") =>
  api.post(`/api/signals/${id}/vote`, { voteType }).then((r) => r.data);
export const removeSignalVote = (id: string) => api.delete(`/api/signals/${id}/vote`);
export const getOpinions = (id: string) => api.get<Opinion[]>(`/api/signals/${id}/opinions`).then((r) => r.data);
export const createOpinion = (id: string, data: { position: OpinionPosition; content: string; sources: SignalSource[] }) =>
  api.post<Opinion>(`/api/signals/${id}/opinions`, data).then((r) => r.data);
export const voteOpinion = (id: string, voteType: "UP" | "DOWN") =>
  api.post<Opinion>(`/api/opinions/${id}/vote`, { voteType }).then((r) => r.data);
export const getComments = (id: string) => api.get<Comment[]>(`/api/signals/${id}/comments`).then((r) => r.data);
export const createComment = (id: string, data: { content: string; opinionId?: string; parentCommentId?: string }) =>
  api.post<Comment>(`/api/signals/${id}/comments`, data).then((r) => r.data);

// AI analysis results
export interface EvidenceItem {
  claim: string;
  evidence_text: string;
  evidence_type: "SUPPORTING" | "CONTRADICTING" | "CONTEXT";
  relevance_score: number;
  source_url?: string;
}
export interface SourceItem { url: string; title?: string; publisher?: string; source_type?: string; }
export interface CommunityAnalysis {
  signal_id: string;
  community_summary: string;
  supporting_arguments: string[];
  opposing_arguments: string[];
  common_arguments: string[];
  minority_arguments: string[];
  unsupported_claims: string[];
  prompt_version?: string;
  model_version?: string;
}
export interface ResearchAnalysis {
  signal_id: string;
  summary: string;
  conclusion: string;
  confidence: number;
  search_queries: string[];
  sources: SourceItem[];
  evidence: EvidenceItem[];
  limitations: string[];
  prompt_version?: string;
  model_version?: string;
}
export interface ComparisonAnalysis {
  signal_id: string;
  summary: string;
  agreement: string[];
  disagreement: string[];
  important_differences: string[];
  strongest_community_argument: { summary: string; source?: string };
  strongest_research_evidence: { summary: string; source?: string };
  missing_community_perspectives: string[];
  missing_research_perspectives: string[];
  community_conclusion?: string;
  research_conclusion?: string;
  prompt_version?: string;
  model_version?: string;
}

export const getCommunityAnalysis = (id: string) =>
  api.get<CommunityAnalysis>(`/api/signals/${id}/community-analysis`).then((r) => r.data);
export const getResearchAnalysis = (id: string) =>
  api.get<ResearchAnalysis>(`/api/signals/${id}/research-analysis`).then((r) => r.data);
export const getComparisonAnalysis = (id: string) =>
  api.get<ComparisonAnalysis>(`/api/signals/${id}/comparison-analysis`).then((r) => r.data);

export const reportContent = (data: { signalId?: string; opinionId?: string; commentId?: string; reason: string; detail?: string }) =>
  api.post("/api/reports", data).then((r) => r.data);
