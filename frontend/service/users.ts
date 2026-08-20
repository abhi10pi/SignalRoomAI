import api from "./api";

export interface CredibilityScore {
  id: string;
  domainName: string;
  domainSlug: string;
  totalSignals: number;
  correctSignals: number;
  totalValidations: number;
  correctValidations: number;
  accuracyScore: number;
  overconfidencePenalty: number;
  finalScore: number;
  updatedAt: string;
}

export interface UserProfile {
  id: string;
  username: string;
  bio: string | null;
  role: string;
  totalSignals: number;
  totalValidations: number;
  credibilityScores: CredibilityScore[];
}

export const getMyProfile = () =>
  api.get<UserProfile>("/api/users/me/profile").then((r) => r.data);

export const getUserProfile = (username: string) =>
  api.get<UserProfile>(`/api/users/${username}`).then((r) => r.data);
