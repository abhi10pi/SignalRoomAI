"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { createSignal, CreateSignalPayload, ResolutionType, Visibility } from "@/service/signals";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";
import api from "@/service/api";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

interface Domain { id: string; name: string; slug: string; }

const inputCls = (mono: { className: string }) =>
  `${mono.className} w-full border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2.5 text-[13px] tracking-wide outline-none focus:border-[#1C2541] transition-colors`;

export default function CreateSignalPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const [domains, setDomains] = useState<Domain[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState<CreateSignalPayload>({
    title: "",
    description: "",
    domainId: "",
    resolutionType: "NEWS_VERIFIABLE",
    resolutionCriteria: "",
    resolutionDate: "",
    visibility: "PUBLIC",
  });

  useEffect(() => {
    if (!isAuthenticated) { router.push("/auth/login"); return; }
    api.get<Domain[]>("/api/domains").then((r) => setDomains(r.data)).catch(() => {});
  }, [isAuthenticated, router]);

  const set = (k: keyof CreateSignalPayload, v: string) =>
    setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const signal = await createSignal({
        ...form,
        resolutionDate: new Date(form.resolutionDate).toISOString().slice(0, 19),
      });
      router.push(`/signals/${signal.id}`);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setError(msg || "Failed to create signal");
    } finally {
      setLoading(false);
    }
  };

  const ic = inputCls(mono);

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />

      <div className="mx-auto max-w-xl px-6 py-10">
        <p className={`${mono.className} mb-1 text-[11px] tracking-widest text-[#7A2E2E]`}>
          NEW SIGNAL
        </p>
        <h1 className="text-3xl font-semibold tracking-tight mb-1">Create Signal</h1>
        <p className="mb-8 text-[14px] text-[#5B6472]">
          Saved as a draft — publish when you&apos;re ready.
        </p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
              TITLE
            </label>
            <input
              required
              value={form.title}
              onChange={(e) => set("title", e.target.value)}
              className={ic}
              placeholder="What is your prediction?"
            />
          </div>

          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
              DESCRIPTION
            </label>
            <textarea
              required
              rows={4}
              value={form.description}
              onChange={(e) => set("description", e.target.value)}
              className={`${ic} resize-none`}
              placeholder="Explain your reasoning and context…"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
                DOMAIN
              </label>
              <select required value={form.domainId} onChange={(e) => set("domainId", e.target.value)} className={ic}>
                <option value="">Select…</option>
                {domains.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </div>
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
                RESOLUTION TYPE
              </label>
              <select value={form.resolutionType} onChange={(e) => set("resolutionType", e.target.value as ResolutionType)} className={ic}>
                <option value="NEWS_VERIFIABLE">News Verifiable</option>
                <option value="QUANTITATIVE">Quantitative</option>
                <option value="SUBJECTIVE">Subjective</option>
              </select>
            </div>
          </div>

          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
              RESOLUTION CRITERIA
            </label>
            <textarea
              required
              rows={3}
              value={form.resolutionCriteria}
              onChange={(e) => set("resolutionCriteria", e.target.value)}
              className={`${ic} resize-none`}
              placeholder="How will this signal be resolved? Be specific."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
                RESOLUTION DATE
              </label>
              <input
                required
                type="datetime-local"
                value={form.resolutionDate}
                onChange={(e) => set("resolutionDate", e.target.value)}
                className={ic}
              />
            </div>
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>
                VISIBILITY
              </label>
              <select value={form.visibility} onChange={(e) => set("visibility", e.target.value as Visibility)} className={ic}>
                <option value="PUBLIC">Public</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>
          </div>

          {error && (
            <div className={`${mono.className} border-l-2 border-[#7A2E2E] bg-[#FBEDEC] px-3 py-2 text-[11px] tracking-wide text-[#7A2E2E]`}>
              {error}
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="submit"
              disabled={loading}
              className={`${mono.className} flex-1 border border-[#1C2541] bg-[#1C2541] py-3 text-[12px] tracking-widest text-[#F5F5F1] transition-colors hover:bg-[#141B32] disabled:opacity-50`}
            >
              {loading ? "SAVING…" : "SAVE AS DRAFT"}
            </button>
            <button
              type="button"
              onClick={() => router.back()}
              className={`${mono.className} border border-[#DEDCD3] px-5 py-3 text-[12px] tracking-widest text-[#5B6472] hover:border-[#1C2541] transition-colors`}
            >
              CANCEL
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
