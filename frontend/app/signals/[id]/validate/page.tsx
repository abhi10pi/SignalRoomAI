"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";
import api from "@/service/api";
import { Outcome, Confidence } from "@/service/validation";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

interface SignalDetail {
  id: string;
  title: string;
  description: string;
  resolutionType: string;
  resolutionCriteria: string;
  resolutionDate: string;
  status: string;
  submitterUsername: string;
  domainName: string;
  createdAt: string;
}

interface ValidationData {
  predictedOutcome: Outcome;
  confidence: Confidence;
  thesis: string;
}

const inputCls = (mono: { className: string }) =>
  `${mono.className} w-full border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2.5 text-[13px] tracking-wide outline-none focus:border-[#1C2541] transition-colors`;

export default function ValidateSignalPage() {
  const router = useRouter();
  const params = useParams();
  const signalId = params.id as string;
  const { isAuthenticated, user } = useAuth();
  
  const [signal, setSignal] = useState<SignalDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [form, setForm] = useState<ValidationData>({
    predictedOutcome: "TRUE",
    confidence: "MEDIUM",
    thesis: "",
  });

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/auth/login");
      return;
    }

    const loadSignal = async () => {
      try {
        const res = await api.get<SignalDetail>(`/api/signals/${signalId}`);
        setSignal(res.data);
      } catch (err) {
        setError("Failed to load signal");
      } finally {
        setLoading(false);
      }
    };

    loadSignal();
  }, [signalId, isAuthenticated, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSubmitting(true);

    try {
      await api.post(`/api/signals/${signalId}/validate`, form);
      setSuccess("Validation submitted successfully!");
      setTimeout(() => router.push(`/signals/${signalId}`), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Failed to submit validation");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#FCFBF8]">
        <Navbar />
        <div className="flex items-center justify-center h-96">
          <p className={serif.className}>Loading...</p>
        </div>
      </div>
    );
  }

  if (!signal) {
    return (
      <div className="min-h-screen bg-[#FCFBF8]">
        <Navbar />
        <div className="flex items-center justify-center h-96">
          <p className={serif.className}>Signal not found</p>
        </div>
      </div>
    );
  }

  const timeRemaining = new Date(signal.resolutionDate) > new Date();

  return (
    <div className="min-h-screen bg-[#FCFBF8]">
      <Navbar />
      <main className="max-w-4xl mx-auto px-4 py-12">
        <div className="mb-8">
          <h1 className={`${serif.className} text-4xl font-bold mb-2`}>
            Validate Signal
          </h1>
          <p className="text-gray-600">
            Submit your prediction for: <strong>{signal.title}</strong>
          </p>
        </div>

        {/* Signal Summary */}
        <div className="border border-[#DEDCD3] rounded p-6 mb-8 bg-white">
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <p className="text-sm text-gray-500 mb-1">Domain</p>
              <p className={`${serif.className} font-semibold`}>
                {signal.domainName}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 mb-1">Resolution Date</p>
              <p className={`${serif.className} font-semibold`}>
                {new Date(signal.resolutionDate).toLocaleDateString()}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 mb-1">Resolution Type</p>
              <p className={`${serif.className} font-semibold`}>
                {signal.resolutionType}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 mb-1">Status</p>
              <p className={`${serif.className} font-semibold`}>
                {signal.status}
              </p>
            </div>
          </div>

          <div className="mb-4">
            <p className="text-sm text-gray-500 mb-2">Resolution Criteria</p>
            <p className="text-sm leading-relaxed">{signal.resolutionCriteria}</p>
          </div>

          <div>
            <p className="text-sm text-gray-500 mb-2">Description</p>
            <p className="text-sm leading-relaxed whitespace-pre-wrap">
              {signal.description}
            </p>
          </div>
        </div>

        {/* Validation Form */}
        <form onSubmit={handleSubmit} className="border border-[#DEDCD3] rounded p-6 bg-white">
          {error && (
            <div className="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">
              {error}
            </div>
          )}
          {success && (
            <div className="mb-4 p-3 bg-green-100 text-green-700 rounded text-sm">
              {success}
            </div>
          )}

          <div className="mb-6">
            <label className={`${mono.className} block text-[11px] font-semibold mb-2 uppercase`}>
              Your Prediction
            </label>
            <select
              value={form.predictedOutcome}
              onChange={(e) =>
                setForm({ ...form, predictedOutcome: e.target.value as Outcome })
              }
              className={inputCls(mono)}
              required
            >
              <option value="TRUE">TRUE (it will happen)</option>
              <option value="FALSE">FALSE (it won't happen)</option>
              <option value="AMBIGUOUS">AMBIGUOUS (unclear)</option>
            </select>
          </div>

          <div className="mb-6">
            <label className={`${mono.className} block text-[11px] font-semibold mb-2 uppercase`}>
              Your Confidence
            </label>
            <select
              value={form.confidence}
              onChange={(e) =>
                setForm({ ...form, confidence: e.target.value as Confidence })
              }
              className={inputCls(mono)}
              required
            >
              <option value="LOW">LOW (60% confidence)</option>
              <option value="MEDIUM">MEDIUM (70% confidence)</option>
              <option value="HIGH">HIGH (85% confidence)</option>
              <option value="CERTAIN">CERTAIN (95% confidence)</option>
            </select>
          </div>

          <div className="mb-6">
            <label className={`${mono.className} block text-[11px] font-semibold mb-2 uppercase`}>
              Your Thesis (Why do you predict this outcome?)
            </label>
            <textarea
              value={form.thesis}
              onChange={(e) => setForm({ ...form, thesis: e.target.value })}
              className={`${inputCls(mono)} min-h-[200px]`}
              placeholder="Explain your reasoning and evidence for this prediction..."
              required
            />
          </div>

          <div className="flex gap-4">
            <button
              type="submit"
              disabled={submitting}
              className="flex-1 bg-[#1C2541] text-[#FCFBF8] px-6 py-3 font-semibold hover:bg-[#2a3555] transition-colors disabled:opacity-50"
            >
              {submitting ? "Submitting..." : "Submit Validation"}
            </button>
            <button
              type="button"
              onClick={() => router.back()}
              className="flex-1 border border-[#DEDCD3] text-[#1C2541] px-6 py-3 font-semibold hover:bg-gray-100 transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>

        {!timeRemaining && (
          <div className="mt-6 p-4 bg-yellow-100 border border-yellow-300 rounded text-yellow-800">
            <p className={`${serif.className} font-semibold mb-1`}>
              Resolution Date Passed
            </p>
            <p className="text-sm">This signal is past its resolution date and may be resolved soon.</p>
          </div>
        )}
      </main>
    </div>
  );
}
