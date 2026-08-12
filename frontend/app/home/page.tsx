"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import {
  getMySignals,
  publishSignal,
  deleteSignal,
  SignalSummary,
} from "@/service/signals";
import { useAuth } from "@/context/AuthContext";
import Navbar from "@/components/Navbar";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

const STATUS_PILL: Record<string, { label: string; cls: string }> = {
  DRAFT:               { label: "DRAFT",              cls: "bg-[#EFEEE8] text-[#5B6472]" },
  PENDING_VALIDATION:  { label: "PENDING",            cls: "bg-[#FDF8E7] text-[#7A6A2E]" },
  VALIDATED:           { label: "VALIDATED",          cls: "bg-[#EAF3EC] text-[#2F5D3A]" },
  REJECTED:            { label: "REJECTED",           cls: "bg-[#FBEDEC] text-[#7A2E2E]" },
  EVALUATED:           { label: "EVALUATED",          cls: "bg-[#E8EBF3] text-[#1C2541]" },
  EXPIRED_UNRESOLVED:  { label: "EXPIRED",            cls: "bg-[#EFEEE8] text-[#5B6472]" },
};

const TABS = ["ALL", "DRAFT", "PENDING_VALIDATION", "VALIDATED", "EVALUATED", "REJECTED"] as const;
type Tab = typeof TABS[number];

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();
  const [signals, setSignals] = useState<SignalSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<Tab>("ALL");
  const [actionId, setActionId] = useState<string | null>(null);
  const [error, setError] = useState("");

  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) { router.push("/auth/login"); return; }
    setMounted(true);
    load();
  }, [isAuthenticated]);

  const load = () => {
    setLoading(true);
    getMySignals()
      .then(setSignals)
      .finally(() => setLoading(false));
  };

  const handlePublish = async (id: string) => {
    setActionId(id);
    setError("");
    try {
      await publishSignal(id);
      load();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setError(msg || "Failed to publish");
    } finally {
      setActionId(null);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Delete this draft? This cannot be undone.")) return;
    setActionId(id);
    setError("");
    try {
      await deleteSignal(id);
      setSignals((prev) => prev.filter((s) => s.id !== id));
    } catch {
      setError("Failed to delete");
    } finally {
      setActionId(null);
    }
  };

  const filtered = activeTab === "ALL"
    ? signals
    : signals.filter((s) => s.status === activeTab);

  const countFor = (tab: Tab) =>
    tab === "ALL" ? signals.length : signals.filter((s) => s.status === tab).length;

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />

      <div className="mx-auto max-w-4xl px-6 py-10">
        {/* Page header */}
        <div className="flex items-end justify-between mb-8">
          <div>
            <p className={`${mono.className} mb-1 text-[11px] tracking-widest text-[#7A2E2E]`}>
              SIGNAL LEDGER
            </p>
            <h1 className="text-3xl font-semibold tracking-tight" suppressHydrationWarning>
              {mounted ? `${user?.username}'s Signals` : "My Signals"}
            </h1>
          </div>
          <Link
            href="/signals/create"
            className={`${mono.className} border border-[#1C2541] bg-[#1C2541] px-4 py-2.5 text-[11px] tracking-widest text-[#F5F5F1] hover:bg-[#141B32] transition-colors`}
          >
            + NEW SIGNAL
          </Link>
        </div>

        {/* Stats row */}
        <div className="grid grid-cols-3 sm:grid-cols-6 gap-3 mb-8">
          {(["DRAFT", "PENDING_VALIDATION", "VALIDATED", "EVALUATED", "REJECTED"] as const).map((s) => {
            const count = signals.filter((sig) => sig.status === s).length;
            const pill = STATUS_PILL[s];
            return (
              <button
                key={s}
                onClick={() => setActiveTab(s)}
                className={`flex flex-col items-center border py-3 px-2 transition-colors ${
                  activeTab === s ? "border-[#1C2541]" : "border-[#DEDCD3] hover:border-[#B9B7AC]"
                } bg-[#FCFBF8]`}
              >
                <span className={`${mono.className} text-xl font-semibold text-[#1C2541]`}>{count}</span>
                <span className={`${mono.className} mt-1 text-[9px] tracking-widest ${pill.cls.split(" ")[1]}`}>
                  {pill.label}
                </span>
              </button>
            );
          })}
          <button
            onClick={() => setActiveTab("ALL")}
            className={`flex flex-col items-center border py-3 px-2 transition-colors ${
              activeTab === "ALL" ? "border-[#1C2541]" : "border-[#DEDCD3] hover:border-[#B9B7AC]"
            } bg-[#FCFBF8]`}
          >
            <span className={`${mono.className} text-xl font-semibold text-[#1C2541]`}>{signals.length}</span>
            <span className={`${mono.className} mt-1 text-[9px] tracking-widest text-[#5B6472]`}>ALL</span>
          </button>
        </div>

        {/* Tab strip */}
        <div className="flex gap-1 border-b border-[#DEDCD3] mb-6 overflow-x-auto">
          {TABS.map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`${mono.className} shrink-0 px-4 py-2 text-[10px] tracking-widest transition-colors ${
                activeTab === tab
                  ? "border-b-2 border-[#1C2541] text-[#1C2541]"
                  : "text-[#5B6472] hover:text-[#1C2541]"
              }`}
            >
              {tab === "PENDING_VALIDATION" ? "PENDING" : tab.replace("_", " ")} ({countFor(tab)})
            </button>
          ))}
        </div>

        {error && (
          <div className={`${mono.className} mb-4 border-l-2 border-[#7A2E2E] bg-[#FBEDEC] px-3 py-2 text-[11px] tracking-wide text-[#7A2E2E]`}>
            {error}
          </div>
        )}

        {/* Signal list */}
        {loading ? (
          <div className="flex flex-col gap-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-20 animate-pulse border border-[#DEDCD3] bg-[#EFEEE8]" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="border border-dashed border-[#DEDCD3] bg-[#FCFBF8] py-16 text-center">
            <p className={`${mono.className} text-[12px] text-[#5B6472] mb-4`}>
              {activeTab === "ALL" ? "NO SIGNALS YET" : `NO ${activeTab.replace("_", " ")} SIGNALS`}
            </p>
            {activeTab === "ALL" && (
              <Link
                href="/signals/create"
                className={`${mono.className} border border-[#1C2541] px-4 py-2 text-[11px] tracking-widest hover:bg-[#1C2541] hover:text-[#F5F5F1] transition-colors`}
              >
                CREATE YOUR FIRST SIGNAL
              </Link>
            )}
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {filtered.map((s) => {
              const pill = STATUS_PILL[s.status];
              const isDraft = s.status === "DRAFT";
              const busy = actionId === s.id;
              return (
                <div
                  key={s.id}
                  className="border border-[#DEDCD3] bg-[#FCFBF8] p-5 hover:border-[#B9B7AC] transition-colors"
                >
                  <div className="flex items-start justify-between gap-4">
                    {/* Left: title + meta */}
                    <div className="flex-1 min-w-0">
                      <Link href={`/signals/${s.id}`} className="group">
                        <h2 className="text-[15px] font-semibold leading-snug group-hover:underline decoration-dotted underline-offset-2 truncate">
                          {s.title}
                        </h2>
                      </Link>
                      <div className={`${mono.className} mt-1.5 flex flex-wrap gap-x-3 gap-y-1 text-[10px] tracking-wide text-[#5B6472]`}>
                        <span>{s.domainName}</span>
                        <span>·</span>
                        <span>
                          resolves{" "}
                          {new Date(s.resolutionDate).toLocaleDateString("en-US", {
                            day: "2-digit", month: "short", year: "numeric",
                          })}
                        </span>
                        {s.submittedAt && (
                          <>
                            <span>·</span>
                            <span>
                              published{" "}
                              {new Date(s.submittedAt).toLocaleDateString("en-US", {
                                day: "2-digit", month: "short", year: "numeric",
                              })}
                            </span>
                          </>
                        )}
                      </div>
                    </div>

                    {/* Right: status pill */}
                    <span className={`${mono.className} shrink-0 rounded-sm px-2 py-0.5 text-[9px] tracking-widest ${pill.cls}`}>
                      {pill.label}
                    </span>
                  </div>

                  {/* Draft actions */}
                  {isDraft && (
                    <div className="mt-4 flex items-center gap-3 border-t border-[#EFEEE8] pt-3">
                      <Link
                        href={`/signals/${s.id}/edit`}
                        className={`${mono.className} text-[10px] tracking-widest text-[#5B6472] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
                      >
                        EDIT
                      </Link>
                      <span className="text-[#DEDCD3]">|</span>
                      <button
                        onClick={() => handlePublish(s.id)}
                        disabled={busy}
                        className={`${mono.className} text-[10px] tracking-widest text-[#2F5D3A] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors disabled:opacity-40`}
                      >
                        {busy ? "PUBLISHING…" : "PUBLISH"}
                      </button>
                      <span className="text-[#DEDCD3]">|</span>
                      <button
                        onClick={() => handleDelete(s.id)}
                        disabled={busy}
                        className={`${mono.className} text-[10px] tracking-widest text-[#7A2E2E] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors disabled:opacity-40`}
                      >
                        DELETE
                      </button>
                    </div>
                  )}

                  {/* Non-draft: view link */}
                  {!isDraft && (
                    <div className="mt-4 border-t border-[#EFEEE8] pt-3">
                      <Link
                        href={`/signals/${s.id}`}
                        className={`${mono.className} text-[10px] tracking-widest text-[#5B6472] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
                      >
                        VIEW SIGNAL →
                      </Link>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
