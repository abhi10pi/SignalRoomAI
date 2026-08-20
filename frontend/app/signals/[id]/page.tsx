"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getSignal, publishSignal, deleteSignal, SignalDetail } from "@/service/signals";
import { getValidations, submitValidation, ValidationRequest, ValidationResponse, resolveSignal, approveSignal, rejectSignal, Outcome } from "@/service/validation";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

const STATUS_PILL: Record<string, { label: string; cls: string }> = {
  DRAFT:               { label: "DRAFT",    cls: "bg-[#EFEEE8] text-[#5B6472]" },
  PENDING_VALIDATION:  { label: "PENDING",  cls: "bg-[#FDF8E7] text-[#7A6A2E]" },
  VALIDATED:           { label: "VALIDATED",cls: "bg-[#EAF3EC] text-[#2F5D3A]" },
  REJECTED:            { label: "REJECTED", cls: "bg-[#FBEDEC] text-[#7A2E2E]" },
  EVALUATED:           { label: "EVALUATED",cls: "bg-[#E8EBF3] text-[#1C2541]" },
  EXPIRED_UNRESOLVED:  { label: "EXPIRED",  cls: "bg-[#EFEEE8] text-[#5B6472]" },
};

export default function SignalDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { user } = useAuth();
  const [signal, setSignal] = useState<SignalDetail | null>(null);
  const [validations, setValidations] = useState<ValidationResponse[]>([]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [submittingValidation, setSubmittingValidation] = useState(false);
  const [validationForm, setValidationForm] = useState<ValidationRequest>({ 
    predictedOutcome: "AMBIGUOUS", 
    confidence: "MEDIUM", 
    thesis: "" 
  });

  useEffect(() => {
    getSignal(id)
      .then((s) => { setSignal(s); })
      .catch(() => setError("Signal not found"));
    
    getValidations(id)
      .then(setValidations)
      .catch(() => setValidations([]));
  }, [id]);

  const isOwner = user?.userId === signal?.submitterId;
  const isDraft = signal?.status === "DRAFT";
  const isConsultant = user?.role === "CONSULTANT" || user?.role === "ADMIN";
  const canValidate = isConsultant && !isOwner && (signal?.status === "PENDING_VALIDATION" || signal?.status === "VALIDATED");
  const canModerate = isConsultant && signal?.status === "PENDING_VALIDATION";
  const canResolve = user?.role === "ADMIN" && signal?.status === "VALIDATED";

  const handlePublish = async () => {
    if (!signal) return;
    setBusy(true);
    setError("");
    try {
      setSignal(await publishSignal(signal.id));
    } catch (e: any) {
      setError(e.response?.data?.message || "Failed to publish");
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async () => {
    if (!signal || !confirm("Delete this draft? This cannot be undone.")) return;
    setBusy(true);
    try {
      await deleteSignal(signal.id);
      router.push("/home");
    } catch {
      setError("Failed to delete");
      setBusy(false);
    }
  };

  const handleValidationSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!signal) return;
    setSubmittingValidation(true);
    try {
      const saved = await submitValidation(signal.id, validationForm);
      setValidations((v) => {
        // Remove old validation if exists and add new one
        const filtered = v.filter(val => val.consultantId !== saved.consultantId);
        return [saved, ...filtered];
      });
      setValidationForm({ predictedOutcome: "AMBIGUOUS", confidence: "MEDIUM", thesis: "" });
      setError("");
    } catch (e: any) {
      setError(e.response?.data?.message || "Failed to submit validation");
    } finally {
      setSubmittingValidation(false);
    }
  };

  const handleResolve = async (outcome: Outcome) => {
    if (!signal) return;
    if (!confirm(`Resolve this signal with outcome: ${outcome}?`)) return;
    setBusy(true);
    try {
      await resolveSignal(signal.id, outcome);
      setSignal(await getSignal(id));
      setError("");
    } catch (e: any) {
      setError(e.response?.data?.message || "Failed to resolve");
    } finally {
      setBusy(false);
    }
  };

  const handleModeration = async (approve: boolean) => {
    if (!signal) return;
    setBusy(true);
    setError("");
    try {
      if (approve) await approveSignal(signal.id);
      else await rejectSignal(signal.id);
      setSignal(await getSignal(id));
    } catch (e: any) {
      setError(e.response?.data?.message || `Failed to ${approve ? "approve" : "reject"}`);
    } finally {
      setBusy(false);
    }
  };

  if (!signal) return (
    <div className={`${serif.className} min-h-screen bg-[#FCFBF8]`}>
      <Navbar />
      <div className="flex items-center justify-center py-32">
        {error ? (
          <p className="text-red-600">{error}</p>
        ) : (
          <p className={mono.className}>Loading signal...</p>
        )}
      </div>
    </div>
  );

  const pill = STATUS_PILL[signal.status];
  const timeRemaining = new Date(signal.resolutionDate) > new Date();

  return (
    <div className={`${serif.className} min-h-screen bg-[#FCFBF8]`}>
      <Navbar />

      <main className="mx-auto max-w-4xl px-6 py-10">
        {/* Status badge */}
        <div className="mb-6 flex items-center gap-2">
          <span className={`${mono.className} px-3 py-1 rounded text-xs font-semibold ${pill.cls}`}>
            {pill.label}
          </span>
          {signal.actualOutcome && (
            <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded text-xs font-semibold">
              RESOLVED: {signal.actualOutcome}
            </span>
          )}
        </div>

        {/* Title and meta */}
        <h1 className="text-4xl font-bold mb-4">{signal.title}</h1>
        
        <div className={`${mono.className} text-sm text-gray-600 mb-8 space-y-1`}>
          <div>by <strong>{signal.submitterUsername}</strong> in <strong>{signal.domainName}</strong></div>
          {signal.submittedAt && (
            <div>Published: {new Date(signal.submittedAt).toLocaleDateString()}</div>
          )}
          <div>Resolution date: {new Date(signal.resolutionDate).toLocaleDateString()}</div>
          <div>Type: {signal.resolutionType}</div>
        </div>

        {/* Description */}
        <div className="bg-white border border-[#DEDCD3] rounded p-6 mb-6">
          <h2 className={`${mono.className} text-xs font-bold mb-3 uppercase`}>Description</h2>
          <p className="text-base leading-relaxed">{signal.description}</p>
        </div>

        {/* Resolution criteria */}
        <div className="bg-white border border-[#DEDCD3] rounded p-6 mb-6">
          <h2 className={`${mono.className} text-xs font-bold mb-3 uppercase`}>Resolution Criteria</h2>
          <p className="text-base leading-relaxed">{signal.resolutionCriteria}</p>
        </div>

        {/* Owner draft actions */}
        {isOwner && isDraft && (
          <div className="bg-white border border-[#DEDCD3] rounded p-6 mb-6">
            <h2 className={`${mono.className} text-xs font-bold mb-4 uppercase`}>Draft Actions</h2>
            <div className="flex gap-3">
              <Link
                href={`/signals/${signal.id}/edit`}
                className="px-4 py-2 border border-[#1C2541] hover:bg-gray-100"
              >
                Edit
              </Link>
              <button
                onClick={handlePublish}
                disabled={busy}
                className="px-4 py-2 bg-[#2F5D3A] text-white hover:bg-[#254A2F] disabled:opacity-50"
              >
                {busy ? "Publishing…" : "Publish"}
              </button>
              <button
                onClick={handleDelete}
                disabled={busy}
                className="px-4 py-2 border border-red-500 text-red-600 hover:bg-red-50 disabled:opacity-50"
              >
                Delete
              </button>
            </div>
          </div>
        )}

        {/* Moderation controls */}
        {canModerate && signal.status === "PENDING_VALIDATION" && (
          <div className="bg-yellow-50 border border-yellow-200 rounded p-4 mb-6">
            <h3 className={`${mono.className} text-xs font-bold mb-3 uppercase`}>Moderate Signal</h3>
            <div className="flex gap-2">
              <button
                onClick={() => handleModeration(true)}
                disabled={busy}
                className="px-4 py-2 bg-green-500 text-white hover:bg-green-600 disabled:opacity-50"
              >
                {busy ? "Processing…" : "Approve for Validation"}
              </button>
              <button
                onClick={() => handleModeration(false)}
                disabled={busy}
                className="px-4 py-2 bg-red-500 text-white hover:bg-red-600 disabled:opacity-50"
              >
                Reject
              </button>
            </div>
          </div>
        )}

        {/* Validation form for consultants */}
        {canValidate && (
          <div className="bg-white border border-[#DEDCD3] rounded p-6 mb-6">
            <h2 className={`${mono.className} text-xs font-bold mb-4 uppercase`}>Submit Your Validation</h2>
            <form onSubmit={handleValidationSubmit}>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label className={`${mono.className} text-xs font-bold block mb-2`}>Prediction</label>
                  <select
                    value={validationForm.predictedOutcome}
                    onChange={(e) => setValidationForm((f) => ({ ...f, predictedOutcome: e.target.value as Outcome }))}
                    className="w-full border border-[#DEDCD3] p-2"
                    required
                  >
                    <option value="TRUE">TRUE</option>
                    <option value="FALSE">FALSE</option>
                    <option value="AMBIGUOUS">AMBIGUOUS</option>
                  </select>
                </div>
                <div>
                  <label className={`${mono.className} text-xs font-bold block mb-2`}>Confidence</label>
                  <select
                    value={validationForm.confidence}
                    onChange={(e) => setValidationForm((f) => ({ ...f, confidence: e.target.value as any }))}
                    className="w-full border border-[#DEDCD3] p-2"
                    required
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CERTAIN">CERTAIN</option>
                  </select>
                </div>
              </div>
              <div className="mb-4">
                <label className={`${mono.className} text-xs font-bold block mb-2`}>Your Thesis</label>
                <textarea
                  value={validationForm.thesis}
                  onChange={(e) => setValidationForm((f) => ({ ...f, thesis: e.target.value }))}
                  placeholder="Explain your reasoning and evidence…"
                  className="w-full border border-[#DEDCD3] p-2 min-h-[150px]"
                  required
                />
              </div>
              {error && <p className="text-red-600 text-sm mb-4">{error}</p>}
              <button
                type="submit"
                disabled={submittingValidation || !timeRemaining}
                className="px-4 py-2 bg-[#1C2541] text-white hover:bg-[#2a3555] disabled:opacity-50"
              >
                {submittingValidation ? "Submitting…" : "Submit Validation"}
              </button>
              {!timeRemaining && <p className="text-orange-600 text-sm mt-2">Resolution date has passed</p>}
            </form>
          </div>
        )}

        {/* Admin resolution */}
        {canResolve && (
          <div className="bg-blue-50 border border-blue-200 rounded p-6 mb-6">
            <h2 className={`${mono.className} text-xs font-bold mb-3 uppercase`}>Admin Resolution</h2>
            <p className="text-sm mb-4">Choose final outcome based on consultant validations:</p>
            <div className="flex gap-2">
              {(["TRUE", "FALSE", "AMBIGUOUS"] as Outcome[]).map((outcome) => (
                <button
                  key={outcome}
                  onClick={() => handleResolve(outcome)}
                  disabled={busy}
                  className="px-4 py-2 border border-blue-500 text-blue-600 hover:bg-blue-100 disabled:opacity-50"
                >
                  {outcome}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Validations list */}
        <div className="bg-white border border-[#DEDCD3] rounded p-6">
          <h2 className={`${mono.className} text-xs font-bold mb-4 uppercase`}>
            Consultant Validations ({validations.length})
          </h2>
          
          {validations.length === 0 ? (
            <p className="text-gray-500">No validations yet</p>
          ) : (
            <div className="space-y-4">
              {validations.map((v) => (
                <div key={v.id} className="border border-gray-200 p-4 rounded">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <p className="font-semibold">{v.consultantUsername}</p>
                      <p className={`${mono.className} text-xs text-gray-500`}>
                        {new Date(v.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-lg">{v.predictedOutcome}</p>
                      <p className={`${mono.className} text-xs text-gray-500`}>{v.confidence}</p>
                    </div>
                  </div>
                  <div className="text-sm text-gray-700 mb-2">
                    {v.wasCorrect !== null && (
                      <p className={v.wasCorrect ? "text-green-600" : "text-red-600"}>
                        {v.wasCorrect ? "✓ Correct" : "✗ Incorrect"}
                      </p>
                    )}
                  </div>
                  <p className="text-sm">{v.thesis}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Back link */}
        <div className="mt-8">
          <Link
            href="/signals"
            className="text-[#1C2541] hover:underline"
          >
            ← Back to signals
          </Link>
        </div>
      </main>
    </div>
  );
}


