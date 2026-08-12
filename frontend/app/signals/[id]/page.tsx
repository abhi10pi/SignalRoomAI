"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getSignal, publishSignal, deleteSignal, SignalDetail } from "@/service/signals";
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
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    getSignal(id).then(setSignal).catch(() => setError("Signal not found"));
  }, [id]);

  const isOwner = user?.userId === signal?.submitterId;
  const isDraft = signal?.status === "DRAFT";

  const handlePublish = async () => {
    if (!signal) return;
    setBusy(true);
    setError("");
    try {
      setSignal(await publishSignal(signal.id));
    } catch (e: unknown) {
      setError((e as { response?: { data?: { error?: string } } })?.response?.data?.error || "Failed to publish");
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

  if (error && !signal) return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1]`}>
      <Navbar />
      <div className="flex items-center justify-center py-32">
        <p className={`${mono.className} text-[12px] text-[#7A2E2E]`}>{error}</p>
      </div>
    </div>
  );

  if (!signal) return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1]`}>
      <Navbar />
      <div className="flex items-center justify-center py-32">
        <div className="flex flex-col items-center gap-3">
          <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#DEDCD3] border-t-[#1C2541]" />
          <p className={`${mono.className} text-[11px] tracking-widest text-[#5B6472]`}>LOADING</p>
        </div>
      </div>
    </div>
  );

  const pill = STATUS_PILL[signal.status];

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />

      <div className="mx-auto max-w-2xl px-6 py-10">
        {/* Breadcrumb */}
        <div className={`${mono.className} mb-6 flex items-center gap-2 text-[10px] tracking-widest text-[#5B6472]`}>
          <Link href="/signals" className="hover:text-[#1C2541] transition-colors">FEED</Link>
          <span>/</span>
          <Link href={`/signals/domain/${signal.domainSlug}`} className="hover:text-[#1C2541] transition-colors">
            {signal.domainName.toUpperCase()}
          </Link>
          <span>/</span>
          <span className="text-[#1C2541]">SIGNAL</span>
        </div>

        {/* Status + domain */}
        <div className="flex items-center gap-3 mb-5">
          <span className={`${mono.className} rounded-sm px-2 py-0.5 text-[9px] tracking-widest ${pill.cls}`}>
            {pill.label}
          </span>
          <span className={`${mono.className} text-[10px] tracking-widest text-[#5B6472]`}>
            {signal.resolutionType.replace(/_/g, " ")}
          </span>
        </div>

        <h1 className="text-3xl font-semibold tracking-tight leading-snug mb-5">
          {signal.title}
        </h1>

        {/* Meta row */}
        <div className={`${mono.className} flex flex-wrap gap-x-5 gap-y-1 text-[10px] tracking-wide text-[#5B6472] mb-8 pb-6 border-b border-[#DEDCD3]`}>
          <span>by <span className="text-[#1C2541]">{signal.submitterUsername}</span></span>
          {signal.submittedAt && (
            <span>
              published{" "}
              {new Date(signal.submittedAt).toLocaleDateString("en-US", { day: "2-digit", month: "short", year: "numeric" })}
            </span>
          )}
          <span>
            resolves{" "}
            {new Date(signal.resolutionDate).toLocaleDateString("en-US", { day: "2-digit", month: "short", year: "numeric" })}
          </span>
        </div>

        {/* Description */}
        <div className="mb-6">
          <p className={`${mono.className} mb-3 text-[10px] tracking-widest text-[#5B6472]`}>SIGNAL</p>
          <p className="text-[15px] leading-relaxed">{signal.description}</p>
        </div>

        {/* Resolution criteria */}
        <div className="border border-[#DEDCD3] bg-[#FCFBF8] p-6 mb-8">
          <p className={`${mono.className} mb-3 text-[10px] tracking-widest text-[#5B6472]`}>
            RESOLUTION CRITERIA
          </p>
          <p className="text-[14px] leading-relaxed">{signal.resolutionCriteria}</p>
        </div>

        {/* Owner draft actions */}
        {isOwner && isDraft && (
          <div className="border border-[#DEDCD3] bg-[#FCFBF8] p-5">
            <p className={`${mono.className} mb-4 text-[10px] tracking-widest text-[#5B6472]`}>
              DRAFT ACTIONS
            </p>
            <div className="flex flex-wrap gap-3">
              <Link
                href={`/signals/${signal.id}/edit`}
                className={`${mono.className} border border-[#1C2541] px-4 py-2.5 text-[11px] tracking-widest hover:bg-[#EFEEE8] transition-colors`}
              >
                EDIT DRAFT
              </Link>
              <button
                onClick={handlePublish}
                disabled={busy}
                className={`${mono.className} border border-[#2F5D3A] bg-[#2F5D3A] px-4 py-2.5 text-[11px] tracking-widest text-white hover:bg-[#254A2F] transition-colors disabled:opacity-50`}
              >
                {busy ? "PUBLISHING…" : "PUBLISH SIGNAL"}
              </button>
              <button
                onClick={handleDelete}
                disabled={busy}
                className={`${mono.className} border border-[#7A2E2E] px-4 py-2.5 text-[11px] tracking-widest text-[#7A2E2E] hover:bg-[#7A2E2E] hover:text-white transition-colors disabled:opacity-50`}
              >
                DELETE DRAFT
              </button>
            </div>
            {error && (
              <p className={`${mono.className} mt-3 text-[11px] text-[#7A2E2E]`}>{error}</p>
            )}
          </div>
        )}

        {/* Back link for non-owners */}
        {(!isOwner || !isDraft) && (
          <Link
            href="/signals"
            className={`${mono.className} text-[11px] tracking-widest text-[#5B6472] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
          >
            ← BACK TO FEED
          </Link>
        )}
      </div>
    </div>
  );
}
