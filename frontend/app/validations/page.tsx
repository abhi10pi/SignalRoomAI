"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";
import { getMyValidations, ValidationResponse } from "@/service/validation";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

interface ValidationWithSignal extends ValidationResponse {
  signalTitle: string;
  signalId: string;
}

export default function MyValidationsPage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();
  const [validations, setValidations] = useState<ValidationWithSignal[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState<"all" | "correct" | "incorrect" | "unresolved">("all");

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/auth/login");
      return;
    }

    if (user?.role !== "CONSULTANT" && user?.role !== "ADMIN") {
      router.push("/home");
      return;
    }

    loadValidations();
  }, [isAuthenticated, user]);

  const loadValidations = async () => {
    try {
      setLoading(true);
      const data = await getMyValidations();
      setValidations(data as ValidationWithSignal[]);
      setError("");
    } catch (err) {
      setError("Failed to load validations");
    } finally {
      setLoading(false);
    }
  };

  const filteredValidations = validations.filter((v) => {
    if (filter === "all") return true;
    if (filter === "correct") return v.wasCorrect === true;
    if (filter === "incorrect") return v.wasCorrect === false;
    if (filter === "unresolved") return v.wasCorrect === null;
    return true;
  });

  const stats = {
    total: validations.length,
    correct: validations.filter((v) => v.wasCorrect === true).length,
    incorrect: validations.filter((v) => v.wasCorrect === false).length,
    unresolved: validations.filter((v) => v.wasCorrect === null).length,
  };

  const accuracy =
    stats.correct + stats.incorrect > 0
      ? ((stats.correct / (stats.correct + stats.incorrect)) * 100).toFixed(1)
      : "—";

  return (
    <div className={`${serif.className} min-h-screen bg-[#FCFBF8]`}>
      <Navbar />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <h1 className="text-4xl font-bold mb-2">My Validations</h1>
        <p className="text-gray-600 mb-8">Track your signal predictions and accuracy</p>

        {/* Stats */}
        <div className="grid grid-cols-4 gap-4 mb-8">
          <div className="bg-white border border-[#DEDCD3] rounded p-4 text-center">
            <p className={`${mono.className} text-xs text-gray-600 uppercase tracking-widest`}>Total</p>
            <p className="text-3xl font-bold mt-2">{stats.total}</p>
          </div>
          <div className="bg-white border border-green-200 rounded p-4 text-center">
            <p className={`${mono.className} text-xs text-green-700 uppercase tracking-widest`}>Correct</p>
            <p className="text-3xl font-bold text-green-600 mt-2">{stats.correct}</p>
          </div>
          <div className="bg-white border border-red-200 rounded p-4 text-center">
            <p className={`${mono.className} text-xs text-red-700 uppercase tracking-widest`}>Incorrect</p>
            <p className="text-3xl font-bold text-red-600 mt-2">{stats.incorrect}</p>
          </div>
          <div className="bg-white border border-blue-200 rounded p-4 text-center">
            <p className={`${mono.className} text-xs text-blue-700 uppercase tracking-widest`}>Accuracy</p>
            <p className="text-3xl font-bold text-blue-600 mt-2">{accuracy}%</p>
          </div>
        </div>

        {/* Filter */}
        <div className="mb-6 flex flex-wrap gap-2">
          {(["all", "correct", "incorrect", "unresolved"] as const).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 rounded text-sm font-semibold ${
                filter === f
                  ? "bg-[#1C2541] text-white"
                  : "bg-white border border-[#DEDCD3] text-[#1C2541] hover:bg-gray-50"
              }`}
            >
              {f === "all"
                ? "All"
                : f === "correct"
                ? "✓ Correct"
                : f === "incorrect"
                ? "✗ Incorrect"
                : "Unresolved"}
            </button>
          ))}
        </div>

        {error && (
          <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded mb-6">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <p className={mono.className}>Loading validations...</p>
          </div>
        ) : filteredValidations.length === 0 ? (
          <div className="bg-white border border-[#DEDCD3] rounded p-6 text-center">
            <p className="text-gray-500">
              {filter === "all"
                ? "You haven't submitted any validations yet"
                : `No ${filter} validations`}
            </p>
            <Link href="/signals" className="text-blue-600 hover:underline mt-2 inline-block">
              Browse signals to validate
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredValidations.map((v) => (
              <div
                key={v.id}
                className="bg-white border border-[#DEDCD3] rounded p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <Link
                      href={`/signals/${v.signalId}`}
                      className="text-lg font-semibold text-[#1C2541] hover:underline"
                    >
                      {v.signalTitle}
                    </Link>
                    <div className={`${mono.className} text-xs text-gray-600 mt-1`}>
                      {new Date(v.createdAt).toLocaleDateString()} at{" "}
                      {new Date(v.createdAt).toLocaleTimeString()}
                    </div>
                  </div>
                  <div className="text-right">
                    {v.wasCorrect !== null && (
                      <div
                        className={`px-3 py-1 rounded text-sm font-semibold ${
                          v.wasCorrect
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {v.wasCorrect ? "✓ Correct" : "✗ Incorrect"}
                      </div>
                    )}
                    {v.wasCorrect === null && (
                      <div className="bg-gray-100 text-gray-800 px-3 py-1 rounded text-sm font-semibold">
                        Unresolved
                      </div>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4 mb-3">
                  <div>
                    <p className={`${mono.className} text-xs text-gray-600 uppercase tracking-widest`}>
                      Prediction
                    </p>
                    <p className="text-lg font-bold text-[#1C2541]">{v.predictedOutcome}</p>
                  </div>
                  <div>
                    <p className={`${mono.className} text-xs text-gray-600 uppercase tracking-widest`}>
                      Confidence
                    </p>
                    <p className="text-lg font-bold text-[#1C2541]">{v.confidence}</p>
                  </div>
                </div>

                <div className="bg-gray-50 p-3 rounded mb-3">
                  <p className={`${mono.className} text-xs text-gray-600 uppercase tracking-widest mb-2`}>
                    Your Thesis
                  </p>
                  <p className="text-sm leading-relaxed">{v.thesis}</p>
                </div>

                <Link
                  href={`/signals/${v.signalId}`}
                  className="text-blue-600 hover:underline text-sm font-semibold"
                >
                  View Signal Details →
                </Link>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
