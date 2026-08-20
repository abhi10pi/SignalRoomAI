import api from "./api";

export type Outcome = "TRUE" | "FALSE" | "AMBIGUOUS";
export type Confidence = "LOW" | "MEDIUM" | "HIGH" | "CERTAIN";

export interface ValidationResponse {
  id: string;
  signalId: string;
  consultantId: string;
  consultantUsername: string;
  predictedOutcome: Outcome;
  confidence: Confidence;
  thesis: string;
  wasCorrect: boolean | null;
  createdAt: string;
}

export interface ValidationRequest {
  predictedOutcome: Outcome;
  confidence: Confidence;
  thesis: string;
}

export const submitValidation = (signalId: string, data: ValidationRequest) =>
  api.post<ValidationResponse>(`/api/signals/${signalId}/validate`, data).then((r) => r.data);

export const getValidations = (signalId: string) =>
  api.get<ValidationResponse[]>(`/api/signals/${signalId}/validations`).then((r) => r.data);

export const approveSignal = (signalId: string) =>
  api.post(`/api/signals/${signalId}/approve`);

export const rejectSignal = (signalId: string) =>
  api.post(`/api/signals/${signalId}/reject`);

export const resolveSignal = (signalId: string, actualOutcome: Outcome) =>
  api.post(`/api/signals/${signalId}/resolve`, { actualOutcome }).then((r) => r.data);

export const getMyValidations = () =>
  api.get<ValidationResponse[]>("/api/signals/my-validations").then((r) => r.data);
