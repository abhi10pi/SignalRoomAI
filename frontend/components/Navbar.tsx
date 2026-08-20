"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { IBM_Plex_Mono, Source_Serif_4 } from "next/font/google";
import { useAuth } from "@/context/AuthContext";
import { useSyncExternalStore } from "react";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const router = useRouter();
  const path = usePathname();
  const mounted = useSyncExternalStore(() => () => {}, () => true, () => false);

  const handleLogout = () => {
    logout();
    router.push("/");
  };

  const navLink = (href: string, label: string) => {
    const active = path === href || path.startsWith(href + "/");
    return (
      <Link
        href={href}
        className={`${mono.className} text-[11px] tracking-widest transition-colors ${
          active
            ? "text-[#1C2541] border-b border-[#1C2541] pb-0.5"
            : "text-[#5B6472] hover:text-[#1C2541]"
        }`}
      >
        {label}
      </Link>
    );
  };

  return (
    <header className="sticky top-0 z-10 flex items-center justify-between border-b border-[#DEDCD3] bg-[#F5F5F1] px-6 py-4 sm:px-10">
      {/* Logo */}
      <Link href={mounted && isAuthenticated ? "/home" : "/"} className={`${serif.className} flex items-center gap-2.5`}>
        <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
          <circle cx="11" cy="11" r="9.5" stroke="#1C2541" strokeWidth="1" />
          <path d="M6 12.5 L9.5 9 L12.5 12 L16 7" stroke="#7A2E2E" strokeWidth="1.3" fill="none" />
        </svg>
        <span className="text-[15px] font-semibold tracking-wide">Signalroom</span>
      </Link>

      {/* Nav links */}
      <nav className="flex items-center gap-6">
        {mounted && isAuthenticated ? (
          <>
            {navLink("/home", "MY SIGNALS")}
            {navLink("/signals", "PUBLIC FEED")}
                        {user?.role === "CONSULTANT" && navLink("/validations", "MY VALIDATIONS")}
                        {user?.role === "ADMIN" && navLink("/admin", "ADMIN PANEL")}
            <Link
              href="/signals/create"
              className={`${mono.className} border border-[#1C2541] px-3 py-1.5 text-[11px] tracking-widest hover:bg-[#1C2541] hover:text-[#F5F5F1] transition-colors`}
            >
              + NEW
            </Link>
            <div className="flex items-center gap-3 border-l border-[#DEDCD3] pl-5">
              <span className={`${mono.className} text-[10px] tracking-widest text-[#5B6472]`}>
                {user?.username}
              </span>
              <button
                onClick={handleLogout}
                className={`${mono.className} text-[11px] tracking-widest text-[#7A2E2E] underline decoration-dotted underline-offset-4 hover:text-[#1C2541] transition-colors`}
              >
                SIGN OUT
              </button>
            </div>
          </>
        ) : (
          <>
            {navLink("/signals", "FEED")}
            <Link
              href="/auth/login"
              className={`${mono.className} border border-[#1C2541] bg-[#1C2541] px-3 py-1.5 text-[11px] tracking-widest text-[#F5F5F1] hover:bg-[#141B32] transition-colors`}
            >
              SIGN IN
            </Link>
          </>
        )}
      </nav>
    </header>
  );
}
