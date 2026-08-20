"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getPublicFeed, searchSignals, SignalSummary } from "@/service/signals";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

const STATUS_PILL: Record<string, { label: string; cls: string }> = {
  PENDING_VALIDATION: { label: "PENDING",   cls: "bg-[#FDF8E7] text-[#7A6A2E]" },
  VALIDATED:          { label: "VALIDATED", cls: "bg-[#EAF3EC] text-[#2F5D3A]" },
  REJECTED:           { label: "REJECTED",  cls: "bg-[#FBEDEC] text-[#7A2E2E]" },
  EVALUATED:          { label: "EVALUATED", cls: "bg-[#E8EBF3] text-[#1C2541]" },
  EXPIRED_UNRESOLVED: { label: "EXPIRED",   cls: "bg-[#EFEEE8] text-[#5B6472]" },
};

export default function PublicFeedPage() {
  const { isAuthenticated } = useAuth();
  const [signals, setSignals] = useState<SignalSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState("newest");
  const [query, setQuery] = useState("");
  const [activeSearch, setActiveSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const req = activeSearch
      ? searchSignals(activeSearch, page)
      : getPublicFeed(page, 20, sort);
    req
      .then((data) => { setSignals(data.content); setTotalPages(data.totalPages); setError(""); })
      .catch(() => setError("The public feed is temporarily unavailable."))
      .finally(() => setLoading(false));
  }, [page, sort, activeSearch]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    setActiveSearch(query);
  };

  const clearSearch = () => {
    setQuery("");
    setActiveSearch("");
    setPage(0);
  };

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />

      <div className="mx-auto max-w-4xl px-6 py-10">
        {/* Header */}
        <div className="mb-8">
          <p className={`${mono.className} mb-1 text-[11px] tracking-widest text-[#7A2E2E]`}>
            PUBLIC SIGNALS
          </p>
          <div className="flex items-end justify-between gap-5">
            <div>
              <h1 className="text-3xl font-semibold tracking-tight">Signal Feed</h1>
              <p className="mt-1 text-[14px] text-[#5B6472]">
                Claims, evidence, and accountable predictions from the room.
              </p>
            </div>
            {isAuthenticated && (
              <Link
                href="/signals/create"
                className={`${mono.className} shrink-0 border border-[#1C2541] bg-[#1C2541] px-3 py-2 text-[10px] tracking-widest text-white hover:bg-[#141B32] transition-colors`}
              >
                + POST SIGNAL
              </Link>
            )}
          </div>
        </div>

        {/* Search + Sort bar */}
        <div className="flex flex-col sm:flex-row gap-3 mb-8">
          <form onSubmit={handleSearch} className="flex flex-1 gap-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search by title or description…"
              className={`${mono.className} flex-1 border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2 text-[12px] tracking-wide outline-none focus:border-[#1C2541] transition-colors`}
            />
            <button
              type="submit"
              className={`${mono.className} border border-[#1C2541] px-4 py-2 text-[11px] tracking-widest hover:bg-[#1C2541] hover:text-[#F5F5F1] transition-colors`}
            >
              SEARCH
            </button>
            {activeSearch && (
              <button
                type="button"
                onClick={clearSearch}
                className={`${mono.className} border border-[#DEDCD3] px-3 py-2 text-[11px] tracking-widest text-[#5B6472] hover:border-[#1C2541] transition-colors`}
              >
                ✕
              </button>
            )}
          </form>
          <select
            value={sort}
            onChange={(e) => { setSort(e.target.value); setPage(0); }}
            className={`${mono.className} border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2 text-[11px] tracking-widest`}
          >
            <option value="newest">NEWEST FIRST</option>
            <option value="oldest">OLDEST FIRST</option>
          </select>
        </div>

        {activeSearch && (
          <p className={`${mono.className} mb-4 text-[11px] tracking-wide text-[#5B6472]`}>
            Results for &ldquo;{activeSearch}&rdquo; — {signals.length} found
          </p>
        )}

        {error && (
          <div className={`${mono.className} mb-4 border-l-2 border-[#7A2E2E] bg-[#FBEDEC] px-3 py-2 text-[11px] text-[#7A2E2E]`}>
            {error}
          </div>
        )}

        {/* List */}
        {loading ? (
          <div className="flex flex-col gap-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-24 animate-pulse border border-[#DEDCD3] bg-[#EFEEE8]" />
            ))}
          </div>
        ) : signals.length === 0 ? (
          <div className="border border-dashed border-[#DEDCD3] bg-[#FCFBF8] py-16 text-center">
            <p className={`${mono.className} text-[12px] text-[#5B6472]`}>
              {activeSearch ? "NO SIGNALS MATCH YOUR SEARCH" : "NO PUBLIC SIGNALS YET"}
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {signals.map((s) => {
              const pill = STATUS_PILL[s.status];
              const initials = s.submitterUsername.slice(0, 2).toUpperCase();
              return (
                <Link
                  key={s.id}
                  href={`/signals/${s.id}`}
                  className="block border border-[#DEDCD3] bg-[#FCFBF8] p-5 hover:border-[#1C2541] transition-colors group"
                >
                  <div className="flex items-center justify-between gap-4 border-b border-[#EFEEE8] pb-3">
                    <div className="flex min-w-0 items-center gap-3">
                      <span className={`${mono.className} flex h-8 w-8 shrink-0 items-center justify-center bg-[#E8EBF3] text-[10px] text-[#1C2541]`}>
                        {initials}
                      </span>
                      <div className="min-w-0">
                        <p className="truncate text-[13px] font-semibold">{s.submitterUsername}</p>
                        <p className={`${mono.className} text-[10px] tracking-wide text-[#5B6472]`}>
                          {s.domainName} · {new Date(s.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric" })}
                        </p>
                      </div>
                    </div>
                    {pill && <span className={`${mono.className} shrink-0 rounded-sm px-2 py-0.5 text-[9px] tracking-widest ${pill.cls}`}>{pill.label}</span>}
                  </div>
                  <div className="pt-4">
                    <h2 className="text-[17px] font-semibold leading-snug group-hover:underline decoration-dotted underline-offset-2">{s.title}</h2>
                  </div>
                  <div className={`${mono.className} mt-2 flex flex-wrap gap-x-4 gap-y-1 text-[10px] tracking-wide text-[#5B6472]`}>
                    <span>
                      resolves{" "}
                      {new Date(s.resolutionDate).toLocaleDateString("en-US", {
                        day: "2-digit", month: "short", year: "numeric",
                      })}
                    </span>
                    <span className="ml-auto text-[#1C2541]">OPEN SIGNAL →</span>
                  </div>
                </Link>
              );
            })}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className={`${mono.className} mt-8 flex items-center justify-center gap-6 text-[11px] tracking-widest`}>
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
              className="disabled:opacity-30 hover:text-[#7A2E2E] transition-colors"
            >
              ← PREV
            </button>
            <span className="text-[#5B6472]">
              PAGE {page + 1} OF {totalPages}
            </span>
            <button
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
              className="disabled:opacity-30 hover:text-[#7A2E2E] transition-colors"
            >
              NEXT →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
