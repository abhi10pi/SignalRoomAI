"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { createSignal, CreateSignalPayload } from "@/service/signals";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

const inputCls = (mono: { className: string }) =>
  `${mono.className} w-full border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2.5 text-[13px] tracking-wide outline-none focus:border-[#1C2541] transition-colors`;

export default function CreateSignalPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState<CreateSignalPayload>({
    title: "",
    description: "",
    category: "Technology",
    tags: [],
    sources: [],
  });

  if (!isAuthenticated) { router.push("/auth/login"); }

  const set = (k: keyof CreateSignalPayload, v: string | string[]) =>
    setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const signal = await createSignal(form);
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
          Discussion stays open for seven days, then is archived for everyone to read.
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
              placeholder="State a claim, question, or opinion"
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

          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>CATEGORY</label>
            <select required value={form.category} onChange={(e) => set("category", e.target.value)} className={ic}>
              {['Technology', 'AI', 'Science', 'Finance', 'Business', 'Society', 'Politics', 'Health', 'Environment', 'Other'].map((category) => <option key={category}>{category}</option>)}
            </select>
          </div>
          <div>
            <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>TAGS</label>
            <input value={form.tags.join(", ")} onChange={(e) => set("tags", e.target.value.split(",").map((tag) => tag.trim()).filter(Boolean))} className={ic} placeholder="remote-work, productivity" />
          </div>
          <div>
            <div>
              <label className={`${mono.className} mb-1.5 block text-[10px] tracking-widest text-[#5B6472]`}>OPTIONAL SOURCE URL</label>
              <input value={form.sources[0]?.url || ""} onChange={(e) => setForm((f) => ({ ...f, sources: e.target.value ? [{ url: e.target.value }] : [] }))} className={ic} placeholder="https://example.com/source" type="url" />
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
              {loading ? "PUBLISHING…" : "PUBLISH SIGNAL"}
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
