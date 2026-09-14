"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";
import {
  Comment, CommunityAnalysis, ComparisonAnalysis, ResearchAnalysis,
  createComment, createOpinion, getComments, getCommunityAnalysis,
  getComparisonAnalysis, getOpinions, getResearchAnalysis, getSignal,
  Opinion, OpinionPosition, removeSignalVote, reportContent,
  SignalDetail, voteOpinion, voteSignal,
} from "@/service/signals";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });
const input = "w-full border border-[#DEDCD3] bg-[#FCFBF8] px-3 py-2.5 outline-none focus:border-[#1C2541]";

function Thread({ comment, onReply, canWrite }: { comment: Comment; onReply: (id: string, content: string) => void; canWrite: boolean }) {
  const [reply, setReply] = useState("");
  if (comment.hidden) return null;
  return (
    <div className="border-l border-[#DEDCD3] pl-4">
      <p className={`${mono.className} text-xs text-[#5B6472]`}>{comment.username} · {new Date(comment.createdAt).toLocaleString()}</p>
      <p className="mt-1 whitespace-pre-wrap">{comment.content}</p>
      {canWrite && (
        <form className="mt-3 flex gap-2" onSubmit={(e) => { e.preventDefault(); if (reply.trim()) { onReply(comment.id, reply.trim()); setReply(""); } }}>
          <input className={`${input} text-sm`} value={reply} onChange={(e) => setReply(e.target.value)} placeholder="Reply" />
          <button className="border border-[#1C2541] px-3 text-xs" type="submit">Reply</button>
        </form>
      )}
      <div className="mt-4 space-y-4">{comment.replies?.map((child) => <Thread key={child.id} comment={child} onReply={onReply} canWrite={canWrite} />)}</div>
    </div>
  );
}

function EvidenceBadge({ type }: { type: string }) {
  const colors: Record<string, string> = {
    SUPPORTING: "bg-[#EAF3EC] text-[#2F5D3A]",
    CONTRADICTING: "bg-[#FDECEA] text-[#7A2E2E]",
    CONTEXT: "bg-[#F1F0EA] text-[#5B6472]",
  };
  return <span className={`${mono.className} px-2 py-0.5 text-xs font-semibold ${colors[type] ?? "bg-[#F1F0EA]"}`}>{type}</span>;
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="mb-8 border border-[#DEDCD3] bg-white p-6">
      <h2 className="mb-4 text-xl font-semibold">{title}</h2>
      {children}
    </section>
  );
}

function ArgList({ items, label }: { items: string[]; label: string }) {
  if (!items?.length) return null;
  return (
    <div className="mt-3">
      <p className={`${mono.className} mb-1 text-xs font-semibold text-[#5B6472] uppercase`}>{label}</p>
      <ul className="space-y-1">{items.map((a, i) => <li key={i} className="text-sm leading-relaxed">• {a}</li>)}</ul>
    </div>
  );
}

export default function DiscussionSignalPage() {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated } = useAuth();
  const [signal, setSignal] = useState<SignalDetail | null>(null);
  const [opinions, setOpinions] = useState<Opinion[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [community, setCommunity] = useState<CommunityAnalysis | null>(null);
  const [research, setResearch] = useState<ResearchAnalysis | null>(null);
  const [comparison, setComparison] = useState<ComparisonAnalysis | null>(null);
  const [position, setPosition] = useState<OpinionPosition>("AGREE");
  const [opinionText, setOpinionText] = useState("");
  const [commentText, setCommentText] = useState("");
  const [error, setError] = useState("");
  const [reportMsg, setReportMsg] = useState("");
  const [now] = useState(() => Date.now());

  const isClosed = signal?.status === "CLOSED" || signal?.status === "ARCHIVED";
  const canWrite = isAuthenticated && signal?.status === "OPEN";

  const refresh = useCallback(async () => {
    const [nextSignal, nextOpinions, nextComments] = await Promise.all([
      getSignal(id), getOpinions(id), getComments(id),
    ]);
    setSignal(nextSignal);
    setOpinions(nextOpinions);
    setComments(nextComments);
  }, [id]);

  const loadAiResults = useCallback(async () => {
    try {
      const [c, r, comp] = await Promise.all([
        getCommunityAnalysis(id),
        getResearchAnalysis(id),
        getComparisonAnalysis(id),
      ]);
      setCommunity(c);
      setResearch(r);
      setComparison(comp);
    } catch {
      // AI results not yet available — silently skip
    }
  }, [id]);

  useEffect(() => {
    const load = async () => {
      try {
        await refresh();
      } catch {
        setError("Signal not found");
      }
    };
    void load();
  }, [refresh]);

  useEffect(() => {
    if (isClosed) void loadAiResults();
  }, [isClosed, loadAiResults]);

  const submit = async (e: FormEvent, action: () => Promise<void>) => {
    e.preventDefault();
    setError("");
    try { await action(); await refresh(); } catch { setError("That action could not be completed."); }
  };

  const vote = async (voteType: "UP" | "DOWN") => {
    if (!signal || !canWrite) return;
    try {
      if (signal.myVote === voteType) await removeSignalVote(id);
      else await voteSignal(id, voteType);
      await refresh();
    } catch { setError("Voting is unavailable."); }
  };

  const report = async (opinionId?: string, commentId?: string) => {
    try {
      await reportContent({ opinionId, commentId, signalId: opinionId || commentId ? undefined : id, reason: "INAPPROPRIATE" });
      setReportMsg("Report submitted.");
      setTimeout(() => setReportMsg(""), 3000);
    } catch { setError("Could not submit report."); }
  };

  if (!signal) return <><Navbar /><main className="mx-auto max-w-4xl px-6 py-20">{error || "Loading signal..."}</main></>;

  const end = signal.discussionEnd ? new Date(signal.discussionEnd) : null;
  const days = end ? Math.max(0, Math.ceil((end.getTime() - now) / 86400000)) : 0;

  return (
    <div className={`${serif.className} min-h-screen bg-[#FCFBF8] text-[#1C2541]`}>
      <Navbar />
      <main className="mx-auto max-w-4xl px-6 py-10">

        {/* Header */}
        <div className="mb-6 flex items-center gap-3">
          <span className={`${mono.className} bg-[#EAF3EC] px-3 py-1 text-xs font-semibold`}>{signal.status}</span>
          <span className={`${mono.className} text-xs text-[#5B6472]`}>{signal.category}</span>
        </div>
        <h1 className="mb-3 text-4xl font-bold">{signal.title}</h1>
        <p className={`${mono.className} mb-8 text-xs text-[#5B6472]`}>by {signal.authorUsername} · {new Date(signal.createdAt).toLocaleDateString()}</p>

        {/* Signal body */}
        <section className="border border-[#DEDCD3] bg-white p-6 mb-8">
          <p className="whitespace-pre-wrap leading-relaxed">{signal.description}</p>
          {signal.tags?.length ? (
            <div className="mt-5 flex flex-wrap gap-2">
              {signal.tags.map((tag: string) => <span key={tag} className="bg-[#F1F0EA] px-2 py-1 text-xs">#{tag}</span>)}
            </div>
          ) : null}
          {signal.sources?.map((source) => (
            <a className="mt-5 block text-sm underline" href={source.url} target="_blank" rel="noreferrer" key={source.url}>{source.title || source.url}</a>
          ))}
        </section>

        {/* Vote bar */}
        <section className="mb-8 border-y border-[#DEDCD3] py-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <strong className="text-3xl">{signal.upPercent || 0}%</strong> agree
              <span className="mx-2 text-[#B5B2A8]">/</span>
              <strong className="text-3xl">{signal.downPercent || 0}%</strong> disagree
              <p className={`${mono.className} mt-1 text-xs text-[#5B6472]`}>{signal.totalVotes || 0} total votes</p>
            </div>
            {canWrite && (
              <div className="flex gap-2">
                <button className={`border px-4 py-2 ${signal.myVote === "UP" ? "bg-[#2F5D3A] text-white" : "border-[#2F5D3A] text-[#2F5D3A]"}`} onClick={() => vote("UP")}>UPVOTE</button>
                <button className={`border px-4 py-2 ${signal.myVote === "DOWN" ? "bg-[#7A2E2E] text-white" : "border-[#7A2E2E] text-[#7A2E2E]"}`} onClick={() => vote("DOWN")}>DOWNVOTE</button>
              </div>
            )}
          </div>
          {signal.status === "OPEN" && <p className={`${mono.className} mt-4 text-xs text-[#7A2E2E]`}>Discussion closes in about {days} day{days === 1 ? "" : "s"}.</p>}
        </section>

        {/* ── CLOSED / ARCHIVED: AI Results ── */}
        {isClosed && (
          <>
            {/* Community Analysis */}
            {community && (
              <Section title="Community Analysis">
                <p className="leading-relaxed">{community.community_summary}</p>
                <ArgList items={community.supporting_arguments} label="Supporting arguments" />
                <ArgList items={community.opposing_arguments} label="Opposing arguments" />
                <ArgList items={community.common_arguments} label="Common ground" />
                {community.unsupported_claims?.length > 0 && (
                  <ArgList items={community.unsupported_claims} label="Unsupported claims" />
                )}
                {community.model_version && (
                  <p className={`${mono.className} mt-4 text-xs text-[#B5B2A8]`}>Model: {community.model_version} · Prompt: {community.prompt_version}</p>
                )}
              </Section>
            )}

            {/* Research Analysis */}
            {research && (
              <Section title="Research Analysis">
                <div className="mb-3 flex items-center gap-3">
                  <span className={`${mono.className} text-sm font-semibold`}>{research.conclusion}</span>
                  <span className={`${mono.className} text-xs text-[#5B6472]`}>Confidence: {research.confidence}%</span>
                </div>
                <p className="leading-relaxed">{research.summary}</p>

                {research.evidence?.length > 0 && (
                  <div className="mt-4 space-y-3">
                    <p className={`${mono.className} text-xs font-semibold text-[#5B6472] uppercase`}>Evidence</p>
                    {research.evidence.map((ev, i) => (
                      <div key={i} className="border border-[#DEDCD3] p-3">
                        <div className="mb-1 flex items-center gap-2">
                          <EvidenceBadge type={ev.evidence_type} />
                          <span className="text-xs text-[#5B6472]">relevance {Math.round(ev.relevance_score * 100)}%</span>
                        </div>
                        <p className="text-sm font-medium">{ev.claim}</p>
                        <p className="mt-1 text-sm text-[#5B6472]">{ev.evidence_text}</p>
                        {ev.source_url && <a href={ev.source_url} target="_blank" rel="noreferrer" className="mt-1 block text-xs underline">{ev.source_url}</a>}
                      </div>
                    ))}
                  </div>
                )}

                {research.sources?.length > 0 && (
                  <div className="mt-4">
                    <p className={`${mono.className} mb-2 text-xs font-semibold text-[#5B6472] uppercase`}>Sources</p>
                    <ul className="space-y-1">
                      {research.sources.map((s, i) => (
                        <li key={i} className="text-sm">
                          <a href={s.url} target="_blank" rel="noreferrer" className="underline">{s.title || s.url}</a>
                          {s.publisher && <span className="ml-2 text-xs text-[#5B6472]">— {s.publisher}</span>}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {research.limitations?.length > 0 && (
                  <ArgList items={research.limitations} label="Limitations" />
                )}
                {research.model_version && (
                  <p className={`${mono.className} mt-4 text-xs text-[#B5B2A8]`}>Model: {research.model_version} · Prompt: {research.prompt_version}</p>
                )}
              </Section>
            )}

            {/* Comparison Analysis */}
            {comparison && (
              <Section title="Comparison: Community vs Research">
                <p className="leading-relaxed">{comparison.summary}</p>
                <ArgList items={comparison.agreement} label="Agreement" />
                <ArgList items={comparison.disagreement} label="Disagreement" />
                <ArgList items={comparison.important_differences} label="Important differences" />

                <div className="mt-4 grid gap-4 sm:grid-cols-2">
                  <div className="border border-[#DEDCD3] p-4">
                    <p className={`${mono.className} mb-2 text-xs font-semibold text-[#5B6472] uppercase`}>Strongest community argument</p>
                    <p className="text-sm">{comparison.strongest_community_argument?.summary}</p>
                  </div>
                  <div className="border border-[#DEDCD3] p-4">
                    <p className={`${mono.className} mb-2 text-xs font-semibold text-[#5B6472] uppercase`}>Strongest research evidence</p>
                    <p className="text-sm">{comparison.strongest_research_evidence?.summary}</p>
                    {comparison.strongest_research_evidence?.source && (
                      <a href={comparison.strongest_research_evidence.source} target="_blank" rel="noreferrer" className="mt-1 block text-xs underline">{comparison.strongest_research_evidence.source}</a>
                    )}
                  </div>
                </div>

                <ArgList items={comparison.missing_community_perspectives} label="Missing community perspectives" />
                <ArgList items={comparison.missing_research_perspectives} label="Missing research perspectives" />
                {comparison.model_version && (
                  <p className={`${mono.className} mt-4 text-xs text-[#B5B2A8]`}>Model: {comparison.model_version} · Prompt: {comparison.prompt_version}</p>
                )}
              </Section>
            )}

            {/* Archived notice */}
            {signal.status === "ARCHIVED" && (
              <div className="mb-8 border border-[#DEDCD3] bg-[#F1F0EA] p-6 text-center">
                <p className={`${mono.className} text-sm text-[#5B6472]`}>This signal is archived. The discussion is closed and the AI analysis above is the final record.</p>
              </div>
            )}
          </>
        )}

        {/* Community opinions */}
        <section className="mb-10">
          <h2 className="mb-4 text-2xl font-semibold">Community opinions</h2>
          {canWrite && (
            <form className="mb-6 border border-[#DEDCD3] bg-white p-5" onSubmit={(e) => submit(e, async () => { await createOpinion(id, { position, content: opinionText, sources: [] }); setOpinionText(""); })}>
              <div className="mb-3 flex gap-2">
                {(["AGREE", "DISAGREE", "NEUTRAL"] as OpinionPosition[]).map((item) => (
                  <button type="button" key={item} onClick={() => setPosition(item)} className={`border px-3 py-1 text-xs ${position === item ? "bg-[#1C2541] text-white" : ""}`}>{item}</button>
                ))}
              </div>
              <textarea required className={`${input} min-h-24`} value={opinionText} onChange={(e) => setOpinionText(e.target.value)} placeholder="Explain your position and evidence" />
              <button className="mt-3 bg-[#1C2541] px-4 py-2 text-xs text-white" type="submit">Post opinion</button>
            </form>
          )}
          {opinions.length === 0 ? (
            <p className="text-[#5B6472]">No opinions yet.</p>
          ) : (
            <div className="space-y-4">
              {opinions.filter(o => !o.hidden).map((opinion) => (
                <article className="border border-[#DEDCD3] bg-white p-5" key={opinion.id}>
                  <div className="flex justify-between">
                    <strong>{opinion.position}</strong>
                    <span className={`${mono.className} text-xs text-[#5B6472]`}>{opinion.username}</span>
                  </div>
                  <p className="mt-3 whitespace-pre-wrap">{opinion.content}</p>
                  <div className="mt-4 flex gap-3 text-xs">
                    <button onClick={() => voteOpinion(opinion.id, "UP").then(refresh)}>Helpful ↑ {opinion.upVotes}</button>
                    <button onClick={() => voteOpinion(opinion.id, "DOWN").then(refresh)}>↓ {opinion.downVotes}</button>
                    {isAuthenticated && <button className="text-[#7A2E2E]" onClick={() => report(opinion.id)}>Report</button>}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        {/* Discussion */}
        <section>
          <h2 className="mb-4 text-2xl font-semibold">Discussion</h2>
          {canWrite && (
            <form className="mb-6 flex gap-2" onSubmit={(e) => submit(e, async () => { await createComment(id, { content: commentText }); setCommentText(""); })}>
              <input required className={input} value={commentText} onChange={(e) => setCommentText(e.target.value)} placeholder="Add to the discussion" />
              <button className="bg-[#1C2541] px-4 text-xs text-white" type="submit">Post</button>
            </form>
          )}
          <div className="space-y-6">
            {comments.map((comment) => (
              <Thread key={comment.id} comment={comment} canWrite={!!canWrite}
                onReply={(parentCommentId, content) => submit({ preventDefault() {} } as FormEvent, async () => { await createComment(id, { content, parentCommentId }); })} />
            ))}
          </div>
        </section>

        {error && <p className="mt-6 text-sm text-[#7A2E2E]">{error}</p>}
        {reportMsg && <p className="mt-6 text-sm text-[#2F5D3A]">{reportMsg}</p>}
      </main>
    </div>
  );
}
