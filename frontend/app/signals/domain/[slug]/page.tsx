"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getDomainFeed, SignalSummary } from "@/service/signals";
import Navbar from "@/components/Navbar";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

export default function DomainFeedPage() {
  const { slug } = useParams<{ slug: string }>();
  const [signals, setSignals] = useState<SignalSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDomainFeed(slug).then((p) => setSignals(p.content)).finally(() => setLoading(false));
  }, [slug]);

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />
      <div className="mx-auto max-w-2xl px-6 py-10">
        <p className={`${mono.className} mb-1 text-[10px] tracking-widest text-[#7A2E2E]`}>DOMAIN FEED</p>
        <h1 className="text-3xl font-semibold mb-1">{slug.replace(/-/g, " ")}</h1>
        <p className={`${mono.className} mb-8 text-[11px] tracking-wide text-[#5B6472]`}>{signals.length} visible signals</p>
        {loading ? (
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-[#DEDCD3] border-t-[#1C2541]" />
        ) : (
          <div className="flex flex-col gap-4">
            {signals.length === 0 && <p className={`${mono.className} text-[11px] text-[#5B6472]`}>No signals in this domain.</p>}
            {signals.map((s) => (
              <Link key={s.id} href={`/signals/${s.id}`} className="block border border-[#DEDCD3] bg-[#FCFBF8] p-5 hover:border-[#1C2541] transition-colors">
                <div className="flex items-center justify-between">
                  <h3 className="font-medium">{s.title}</h3>
                  <div className={`${mono.className} text-[10px] tracking-wide text-[#5B6472]`}>@{s.submitterUsername}</div>
                </div>
                <p className={`${mono.className} mt-2 text-[10px] tracking-wide text-[#5B6472]`}>
                  {s.status.replace(/_/g, " ")} · resolves {new Date(s.resolutionDate).toLocaleDateString()}
                </p>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
