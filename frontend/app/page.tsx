// "use client";

// import { useEffect, useState } from "react";
// import { useRouter } from "next/navigation";
// import Link from "next/link";
// import api from "@/service/api";
// import { useAuth } from "@/context/AuthContext";

// export default function Home() {
//   const [status, setStatus] = useState<string>("Checking...");
//   const { user, logout, isAuthenticated } = useAuth();
//   const router = useRouter();

//   useEffect(() => {
//     api
//       .get("/api/health")
//       .then((res) => setStatus(res.data.status))
//       .catch(() => setStatus("DOWN"));
//   }, []);

//   const handleLogout = () => {
//     logout();
//     router.push("/auth/login");
//   };

//   return (
//     <main className="flex min-h-screen flex-col items-center justify-center gap-4">
//       <h1 className="text-3xl font-bold">Signalroom</h1>
//       <p className="text-lg text-gray-500">Public Track Record Platform</p>

//       <div className="mt-2 rounded-lg border px-4 py-2">
//         Backend Status: <span className="font-semibold">{status}</span>
//       </div>

//       {isAuthenticated && user ? (
//         <div className="mt-4 flex flex-col items-center gap-3">
//           <p className="text-sm text-gray-600">
//             Signed in as <span className="font-semibold">{user.username}</span>{" "}
//             <span className="text-gray-400">({user.role})</span>
//           </p>
//           <button
//             onClick={handleLogout}
//             className="rounded-lg border px-4 py-2 text-sm font-medium hover:bg-gray-50"
//           >
//             Sign out
//           </button>
//         </div>
//       ) : (
//         <div className="mt-4 flex gap-3">
//           <Link
//             href="/auth/login"
//             className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white hover:bg-gray-800"
//           >
//             Sign in
//           </Link>
//           <Link
//             href="/auth/register"
//             className="rounded-lg border px-4 py-2 text-sm font-medium hover:bg-gray-50"
//           >
//             Sign up
//           </Link>
//         </div>
//       )}
//     </main>
//   );
// }



"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import api from "@/service/api";
import { useAuth } from "@/context/AuthContext";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

type BackendStatus = "checking" | "up" | "down";

export default function Home() {
  const [status, setStatus] = useState<BackendStatus>("checking");
  const [dateStamp, setDateStamp] = useState("");
  const { user, logout, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // Set client-side to avoid a server/client date mismatch on hydration
    setDateStamp(
      new Date()
        .toLocaleDateString("en-US", { day: "2-digit", month: "short", year: "numeric" })
        .toUpperCase()
    );

    api
      .get("/api/health")
      .then((res) => {
        const raw = String(res.data?.status ?? "").toUpperCase();
        setStatus(raw === "UP" || raw === "OK" ? "up" : "down");
      })
      .catch(() => setStatus("down"));
  }, []);

  const handleLogout = () => {
    logout();
    router.push("/auth/login");
  };

  const statusLabel: Record<BackendStatus, string> = {
    checking: "CHECKING…",
    up: "OPERATIONAL",
    down: "UNREACHABLE",
  };
  const statusInk: Record<BackendStatus, string> = {
    checking: "text-[#5B6472]",
    up: "text-[#2F5D3A]",
    down: "text-[#7A2E2E]",
  };

  return (
    <main className={`${serif.className} flex min-h-screen flex-col bg-[#F5F5F1] text-[#1C2541]`}>
      {/* Header rule */}
      <header className="flex items-center justify-between border-b border-[#DEDCD3] px-6 py-4 sm:px-10">
        <div className="flex items-center gap-2.5">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="9.5" stroke="#1C2541" strokeWidth="1" />
            <path d="M6 12.5 L9.5 9 L12.5 12 L16 7" stroke="#7A2E2E" strokeWidth="1.3" fill="none" />
          </svg>
          <span className="text-[15px] font-semibold tracking-wide">Signalroom</span>
        </div>
        <span className={`${mono.className} text-[11px] tracking-widest text-[#8A8D85]`}>
          {dateStamp}
        </span>
      </header>

      {/* Body */}
      <div className="flex flex-1 flex-col items-center justify-center px-6 py-16">
        <p className={`${mono.className} mb-4 text-[11px] tracking-[0.25em] text-[#7A2E2E]`}>
          PUBLIC · PERMANENT · VERIFIABLE
        </p>
        <h1 className="text-4xl font-semibold tracking-tight">Signalroom</h1>
        <p className="mt-3 max-w-sm text-center text-[15px] leading-relaxed text-[#5B6472]">
          A ledger of judgment calls — every stance checked against what actually happened.
        </p>

        {/* Ledger card */}
        <div className="mt-10 w-full max-w-sm border border-[#DEDCD3] bg-[#FCFBF8] p-6">
          <div className={`${mono.className} flex items-baseline gap-2 text-[12px] tracking-wide`}>
            <span>SYSTEM STATUS</span>
            <span className="relative top-[-3px] flex-1 border-b border-dotted border-[#B9B7AC]" />
            <span className={statusInk[status]}>{statusLabel[status]}</span>
          </div>

          <div className="my-5 border-t border-[#DEDCD3]" />

          {isAuthenticated && user ? (
            <div className="flex flex-col gap-4">
              <div className={`${mono.className} flex items-baseline gap-2 text-[12px] tracking-wide`}>
                <span>SIGNED IN AS</span>
                <span className="relative top-[-3px] flex-1 border-b border-dotted border-[#B9B7AC]" />
                <span className="text-[#1C2541]">{user.username}</span>
              </div>
              <div className="flex items-center justify-between">
                <span
                  className={`${mono.className} inline-block -rotate-2 border border-[#7A2E2E] px-2 py-0.5 text-[10px] tracking-widest text-[#7A2E2E]`}
                >
                  {user.role}
                </span>
                <button
                  onClick={handleLogout}
                  className={`${mono.className} text-[11px] tracking-wide text-[#5B6472] underline decoration-dotted underline-offset-4 transition-colors hover:text-[#1C2541]`}
                >
                  SIGN OUT
                </button>
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              <Link
                href="/auth/login"
                className="w-full border border-[#1C2541] bg-[#1C2541] py-2.5 text-center text-[13px] font-semibold tracking-wide text-[#F5F5F1] transition-colors hover:bg-[#141B32]"
              >
                Sign In
              </Link>
              <Link
                href="/auth/register"
                className="w-full border border-[#1C2541] py-2.5 text-center text-[13px] font-semibold tracking-wide text-[#1C2541] transition-colors hover:bg-[#EFEEE8]"
              >
                Register
              </Link>
            </div>
          )}
        </div>

        <p className={`${mono.className} mt-8 text-[10px] tracking-widest text-[#8A8D85]`}>
          NO OPINION UNTESTED · NO CLAIM UNSCORED
        </p>
      </div>
    </main>
  );
}