"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getSignal, updateSignal, SignalDetail, UpdateSignalPayload, ResolutionType, Visibility } from "@/service/signals";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";
import api from "@/service/api";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

interface Domain { id: string; name: string; slug: string; }

const ic = `w-full border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2.5 text-[13px] tracking-wide outline-none focus:border-[#1C2541] transition-colors`;

export default function EditSignalPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();

  const [signal, setSignal] = useState<SignalDetail | null>(null);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState<UpdateSignalPayload>({});

  useEffect(() => {
    if (!isAuthenticated) { router.push("/auth/login"); return; }
    Promise.all([
      getSignal(id),
      api.get<Domain[]>("/api/domains").then((r) => r.data),
    ]).then(([sig, doms]) => {
      if (sig.status !== "DRAFT") { router.push(`/signals/${id}`); return; }
      if (sig.submitterId !== user?.userId) { router.push(`/signals/${id}`); return; }
      setSignal(sig);
      setDomains(doms);
      setForm({
        title: sig.title,
        description: sig.description,
        domainId: sig.domainId,
        resolutionType: sig.resolutionType,
        resolutionCriteria: sig.resolutionCriteria,
        resolutionDate: sig.resolutionDate.slice(0, 16),
        visibility: sig.visibility,
      });
    }).catch(() => setError("Signal not found"));
  }, [id, isAuthenticated, user, router]);

  const set = (k: keyof UpdateSignalPayload, v: string) =>
    setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await updateSignal(id, {
        ...form,
        resolutionDate: form.resolutionDate
          ? new Date(form.resolutionDate as string).toISOString().slice(0, 19)
          : undefined,
      });
      router.push(`/signals/${id}`);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setError(msg || "Failed to update signal");
    } finally {
      setLoading(false);
    }
  };

  if (!signal) return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1]`}>
      <Navbar />
      <div className="flex items-center justify-center py-32">
        {error
          ? <p className={`${mono.className} text-[12px] text-[#7A2E2E]`}>{error}</p>
          : <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#DEDCD3] border-t-[#1C2541]" />
        }
      </div>
    </div>
  );

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />
      <div className="mx-auto max-w-xl px-6 py-10">
        <p className={`${mono.className} mb-1 text-[11px] tracking-widest text-[#7A2E2E]`}>EDIT DRAFT</p>
        <h1 className="text-3xl font-semibold tracking-tight mb-8">Edit Signal</h1>

        <form onSubmit={handleSubmit} className="flex flex-col gap-6">
          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>TITLE</label>
            <input
              required
              value={(form.title as string) || ""}
              onChange={(e) => set("title", e.target.value)}
              className={`${mono.className} ${ic}`}
            />
          </div>

          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>DESCRIPTION</label>
            <textarea
              required
              rows={4}
              value={(form.description as string) || ""}
              onChange={(e) => set("description", e.target.value)}
              className={`${mono.className} ${ic} resize-none`}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>DOMAIN</label>
              <select
                required
                value={(form.domainId as string) || ""}
                onChange={(e) => set("domainId", e.target.value)}
                className={`${mono.className} ${ic}`}
              >
                <option value="">Select…</option>
                {domains.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </div>
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>RESOLUTION TYPE</label>
              <select
                value={(form.resolutionType as string) || ""}
                onChange={(e) => set("resolutionType", e.target.value as ResolutionType)}
                className={`${mono.className} ${ic}`}
              >
                <option value="NEWS_VERIFIABLE">News Verifiable</option>
                <option value="QUANTITATIVE">Quantitative</option>
                <option value="SUBJECTIVE">Subjective</option>
              </select>
            </div>
          </div>

          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>RESOLUTION CRITERIA</label>
            <textarea
              required
              rows={3}
              value={(form.resolutionCriteria as string) || ""}
              onChange={(e) => set("resolutionCriteria", e.target.value)}
              className={`${mono.className} ${ic} resize-none`}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>RESOLUTION DATE</label>
              <input
                required
                type="datetime-local"
                value={(form.resolutionDate as string) || ""}
                onChange={(e) => set("resolutionDate", e.target.value)}
                className={`${mono.className} ${ic}`}
              />
            </div>
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>VISIBILITY</label>
              <select
                value={(form.visibility as string) || "PUBLIC"}
                onChange={(e) => set("visibility", e.target.value as Visibility)}
                className={`${mono.className} ${ic}`}
              >
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
              className={`${mono.className} flex-1 border border-[#1C2541] bg-[#1C2541] py-3 text-[12px] tracking-widest text-[#F5F5F1] hover:bg-[#141B32] transition-colors disabled:opacity-50`}
            >
              {loading ? "SAVING…" : "SAVE CHANGES"}
            </button>
            <button
              type="button"
              onClick={() => router.push(`/signals/${id}`)}
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
