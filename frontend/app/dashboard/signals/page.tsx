"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getMySignals, SignalSummary } from "@/service/signals";
import { useAuth } from "@/context/AuthContext";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

const STATUS_COLORS: Record<string, string> = {
  DRAFT: "text-[#5B6472]",
  PENDING_VALIDATION: "text-[#7A6A2E]",
  VALIDATED: "text-[#2F5D3A]",
  REJECTED: "text-[#7A2E2E]",
  EVALUATED: "text-[#1C2541]",
  EXPIRED_UNRESOLVED: "text-[#5B6472]",
};

const GROUPS: { label: string; statuses: string[] }[] = [
  { label: "DRAFTS", statuses: ["DRAFT"] },
  { label: "PENDING VALIDATION", statuses: ["PENDING_VALIDATION"] },
  { label: "VALIDATED", statuses: ["VALIDATED"] },
  { label: "EVALUATED", statuses: ["EVALUATED", "EXPIRED_UNRESOLVED"] },
  { label: "REJECTED", statuses: ["REJECTED"] },
];

export default function MySignalsPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const [signals, setSignals] = useState<SignalSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) { router.push("/auth/login"); return; }
    getMySignals().then(setSignals).finally(() => setLoading(false));
  }, [isAuthenticated, router]);

  return (
    <main className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <header className="flex items-center justify-between border-b border-[#DEDCD3] px-6 py-4 sm:px-10">
        <Link href="/" className="flex items-center gap-2.5">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="9.5" stroke="#1C2541" strokeWidth="1" />
            <path d="M6 12.5 L9.5 9 L12.5 12 L16 7" stroke="#7A2E2E" strokeWidth="1.3" fill="none" />
          </svg>
          <span className="text-[15px] font-semibold tracking-wide">Signalroom</span>
        </Link>
        <Link
          href="/signals/create"
          className={`${mono.className} border border-[#1C2541] px-3 py-1.5 text-[11px] tracking-widest hover:bg-[#1C2541] hover:text-[#F5F5F1] transition-colors`}
        >
          + NEW SIGNAL
        </Link>
      </header>

      <div className="mx-auto max-w-2xl px-6 py-10">
        <h1 className="text-2xl font-semibold tracking-tight mb-1">My Signals</h1>
        <p className={`${mono.className} text-[11px] tracking-widest text-[#5B6472] mb-8`}>
          YOUR COMPLETE SIGNAL HISTORY
        </p>

        {loading ? (
          <p className={`${mono.className} text-[12px] text-[#5B6472]`}>Loading…</p>
        ) : signals.length === 0 ? (
          <div className="border border-[#DEDCD3] bg-[#FCFBF8] p-8 text-center">
            <p className={`${mono.className} text-[12px] text-[#5B6472] mb-4`}>NO SIGNALS YET</p>
            <Link
              href="/signals/create"
              className={`${mono.className} border border-[#1C2541] px-4 py-2 text-[11px] tracking-widest hover:bg-[#1C2541] hover:text-[#F5F5F1] transition-colors`}
            >
              CREATE YOUR FIRST SIGNAL
            </Link>
          </div>
        ) : (
          <div className="flex flex-col gap-8">
            {GROUPS.map(({ label, statuses }) => {
              const group = signals.filter((s) => statuses.includes(s.status));
              if (group.length === 0) return null;
              return (
                <div key={label}>
                  <p className={`${mono.className} mb-3 text-[10px] tracking-widest text-[#5B6472]`}>
                    {label} ({group.length})
                  </p>
                  <div className="flex flex-col gap-2">
                    {group.map((s) => (
                      <Link
                        key={s.id}
                        href={`/signals/${s.id}`}
                        className="flex items-center justify-between border border-[#DEDCD3] bg-[#FCFBF8] px-5 py-4 hover:border-[#1C2541] transition-colors"
                      >
                        <div>
                          <p className="text-[14px] font-semibold leading-snug">{s.title}</p>
                          <p className={`${mono.className} mt-1 text-[10px] tracking-wide text-[#5B6472]`}>
                            {s.domainName} · resolves {new Date(s.resolutionDate).toLocaleDateString("en-US", { day: "2-digit", month: "short", year: "numeric" })}
                          </p>
                        </div>
                        <span className={`${mono.className} shrink-0 text-[10px] tracking-widest ${STATUS_COLORS[s.status]}`}>
                          {s.status.replace(/_/g, " ")}
                        </span>
                      </Link>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </main>
  );
}
