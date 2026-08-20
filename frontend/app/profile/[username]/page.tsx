"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { getUserProfile, UserProfile } from "@/service/users";
import Navbar from "@/components/Navbar";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

export default function ProfilePage() {
  const { username } = useParams<{ username: string }>();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUserProfile(username).then(setProfile).finally(() => setLoading(false));
  }, [username]);

  return (
    <div className={`${serif.className} min-h-screen bg-[#F5F5F1] text-[#1C2541]`}>
      <Navbar />
      <div className="mx-auto max-w-2xl px-6 py-10">
        {loading ? (
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-[#DEDCD3] border-t-[#1C2541]" />
        ) : !profile ? (
          <p className={`${mono.className} text-[12px] text-[#7A2E2E]`}>User not found</p>
        ) : (
          <div className="border border-[#DEDCD3] bg-[#FCFBF8] p-6">
            <div className="mb-6 flex items-start gap-4 border-b border-[#EFEEE8] pb-6">
              <span className={`${mono.className} flex h-14 w-14 shrink-0 items-center justify-center bg-[#E8EBF3] text-lg text-[#1C2541]`}>
                {profile.username.slice(0, 2).toUpperCase()}
              </span>
              <div>
                <p className={`${mono.className} text-[10px] tracking-widest text-[#7A2E2E]`}>PUBLIC PROFILE</p>
                <h1 className="text-2xl font-semibold">{profile.username}</h1>
                <p className={`${mono.className} mt-1 text-[11px] tracking-wide text-[#5B6472]`}>
                  {profile.totalSignals} signals · {profile.totalValidations} validations
                </p>
              </div>
            </div>
            {profile.bio && <p className="mb-4 text-[13px] text-[#5B6472]">{profile.bio}</p>}

            <div className="mb-4">
              <p className={`${mono.className} text-[11px] tracking-widest text-[#5B6472]`}>CREDIBILITY SCORES</p>
              <div className="mt-3 flex flex-col gap-3">
                {profile.credibilityScores.length === 0 && <p className={`${mono.className} text-[11px] text-[#5B6472]`}>No credibility data yet.</p>}
                {profile.credibilityScores.map((c) => (
                  <div key={c.id} className="border p-3">
                    <div className="flex items-center justify-between gap-3">
                      <div className="font-medium">{c.domainName}</div>
                      <div className={`${mono.className} text-[11px] tracking-wide text-[#1C2541]`}>SCORE {Math.round(c.finalScore * 100)}%</div>
                    </div>
                    <div className="mt-3 h-1.5 bg-[#EFEEE8]">
                      <div className="h-full bg-[#2F5D3A]" style={{ width: `${Math.round(c.finalScore * 100)}%` }} />
                    </div>
                    <div className={`${mono.className} mt-2 text-[10px] tracking-wide text-[#5B6472]`}>
                      {c.correctSignals}/{c.totalSignals} signals · {c.correctValidations}/{c.totalValidations} reviews
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
