"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { useAuth } from "@/context/AuthContext";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated) router.replace("/home");
  }, [isAuthenticated, router]);

  return (
    <main className={`${serif.className} flex min-h-screen flex-col bg-[#F5F5F1] text-[#1C2541]`}>
      <header className="flex items-center justify-between border-b border-[#DEDCD3] px-6 py-4 sm:px-10">
        <div className="flex items-center gap-2.5">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="9.5" stroke="#1C2541" strokeWidth="1" />
            <path d="M6 12.5 L9.5 9 L12.5 12 L16 7" stroke="#7A2E2E" strokeWidth="1.3" fill="none" />
          </svg>
          <span className="text-[15px] font-semibold tracking-wide">Signalroom</span>
        </div>
        <Link
          href="/signals"
          className={`${mono.className} text-[11px] tracking-widest text-[#5B6472] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
        >
          PUBLIC FEED
        </Link>
      </header>

      <div className="flex flex-1 flex-col items-center justify-center px-6 py-16">
        <p className={`${mono.className} mb-4 text-[11px] tracking-[0.25em] text-[#7A2E2E]`}>
          PUBLIC · PERMANENT · VERIFIABLE
        </p>
        <h1 className="text-5xl font-semibold tracking-tight">Signalroom</h1>
        <p className="mt-4 max-w-md text-center text-[16px] leading-relaxed text-[#5B6472]">
          A ledger of judgment calls — every stance checked against what actually happened.
        </p>

        <div className="mt-12 flex flex-col gap-3 w-full max-w-xs">
          <Link
            href="/auth/login"
            className={`${mono.className} w-full border border-[#1C2541] bg-[#1C2541] py-3 text-center text-[12px] tracking-widest text-[#F5F5F1] transition-colors hover:bg-[#141B32]`}
          >
            SIGN IN
          </Link>
          <Link
            href="/auth/register"
            className={`${mono.className} w-full border border-[#1C2541] py-3 text-center text-[12px] tracking-widest text-[#1C2541] transition-colors hover:bg-[#EFEEE8]`}
          >
            CREATE ACCOUNT
          </Link>
          <Link
            href="/signals"
            className={`${mono.className} mt-2 text-center text-[11px] tracking-widest text-[#5B6472] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
          >
            BROWSE WITHOUT ACCOUNT →
          </Link>
        </div>

        <div className="mt-16 grid grid-cols-3 gap-8 max-w-lg w-full text-center">
          {[
            { n: "01", title: "Make a Signal", desc: "State your prediction with a clear resolution criteria." },
            { n: "02", title: "Get Validated", desc: "Consultants review and approve your signal for the ledger." },
            { n: "03", title: "Get Scored", desc: "When the date arrives, your call is checked against reality." },
          ].map(({ n, title, desc }) => (
            <div key={n}>
              <p className={`${mono.className} text-[10px] tracking-widest text-[#7A2E2E] mb-2`}>{n}</p>
              <p className="text-[13px] font-semibold mb-1">{title}</p>
              <p className="text-[12px] leading-relaxed text-[#5B6472]">{desc}</p>
            </div>
          ))}
        </div>

        <p className={`${mono.className} mt-16 text-[10px] tracking-widest text-[#B9B7AC]`}>
          NO OPINION UNTESTED · NO CLAIM UNSCORED
        </p>
      </div>
    </main>
  );
}
