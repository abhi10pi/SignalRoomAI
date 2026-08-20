"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import Navbar from "@/components/Navbar";
import { useAuth } from "@/context/AuthContext";
import { getPendingPromotionRequests, approvePromotionRequest, rejectPromotionRequest, PromotionRequest } from "@/service/admin";
import api from "@/service/api";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

interface SignalAdminView {
  id: string;
  title: string;
  status: string;
  submitterUsername: string;
  domainName: string;
  resolutionDate: string;
  validationCount: number;
}

export default function AdminPage() {
  const router = useRouter();
  const { isAuthenticated, user } = useAuth();
  
  const [promotionRequests, setPromotionRequests] = useState<PromotionRequest[]>([]);
  const [signals, setSignals] = useState<SignalAdminView[]>([]);
  const [loadingPromotions, setLoadingPromotions] = useState(true);
  const [loadingSignals, setLoadingSignals] = useState(true);
  const [activeTab, setActiveTab] = useState<"promotions" | "signals">("signals");

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/auth/login");
      return;
    }

    if (user?.role !== "ADMIN") {
      router.push("/home");
      return;
    }

    loadPromotionRequests();
    loadSignals();
  }, [isAuthenticated, user]);

  const loadPromotionRequests = async () => {
    try {
      const data = await getPendingPromotionRequests();
      setPromotionRequests(data);
    } catch (err) {
      console.error("Failed to load promotion requests");
    } finally {
      setLoadingPromotions(false);
    }
  };

  const loadSignals = async () => {
    try {
      const res = await api.get<SignalAdminView[]>("/api/admin/signals");
      setSignals(res.data);
    } catch (err) {
      console.error("Failed to load signals");
    } finally {
      setLoadingSignals(false);
    }
  };

  const handleApprovePromotion = async (id: string) => {
    try {
      await approvePromotionRequest(id);
      setPromotionRequests((r) => r.filter((x) => x.id !== id));
    } catch (err) {
      console.error("Failed to approve promotion");
    }
  };

  const handleRejectPromotion = async (id: string) => {
    try {
      await rejectPromotionRequest(id);
      setPromotionRequests((r) => r.filter((x) => x.id !== id));
    } catch (err) {
      console.error("Failed to reject promotion");
    }
  };

  // Filter signals by status for quick access
  const pendingValidation = signals.filter((s) => s.status === "PENDING_VALIDATION");
  const readyForResolution = signals.filter((s) => s.status === "VALIDATED");

  return (
    <div className={`${serif.className} min-h-screen bg-[#FCFBF8]`}>
      <Navbar />

      <main className="mx-auto max-w-6xl px-6 py-10">
        <h1 className="text-4xl font-bold mb-8">Admin Dashboard</h1>

        {/* Tabs */}
        <div className="flex gap-4 mb-8 border-b border-[#DEDCD3]">
          <button
            onClick={() => setActiveTab("signals")}
            className={`px-4 py-2 font-semibold border-b-2 ${
              activeTab === "signals"
                ? "border-[#1C2541] text-[#1C2541]"
                : "border-transparent text-gray-500 hover:text-[#1C2541]"
            }`}
          >
            Signals ({pendingValidation.length + readyForResolution.length})
          </button>
          <button
            onClick={() => setActiveTab("promotions")}
            className={`px-4 py-2 font-semibold border-b-2 ${
              activeTab === "promotions"
                ? "border-[#1C2541] text-[#1C2541]"
                : "border-transparent text-gray-500 hover:text-[#1C2541]"
            }`}
          >
            Promotions ({promotionRequests.length})
          </button>
        </div>

        {/* Signals Tab */}
        {activeTab === "signals" && (
          <div>
            {/* Pending Validation */}
            <div className="mb-10">
              <h2 className="text-2xl font-bold mb-4">Pending Validation ({pendingValidation.length})</h2>
              {loadingSignals ? (
                <p className={mono.className}>Loading...</p>
              ) : pendingValidation.length === 0 ? (
                <p className="text-gray-500">No signals pending validation</p>
              ) : (
                <div className="grid gap-4">
                  {pendingValidation.map((signal) => (
                    <div
                      key={signal.id}
                      className="bg-white border border-yellow-200 rounded p-4 hover:shadow-md transition-shadow"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <Link
                            href={`/signals/${signal.id}`}
                            className="text-lg font-semibold text-[#1C2541] hover:underline"
                          >
                            {signal.title}
                          </Link>
                          <div className={`${mono.className} text-sm text-gray-600 mt-1`}>
                            by <strong>{signal.submitterUsername}</strong> in <strong>{signal.domainName}</strong>
                          </div>
                          <p className="text-sm text-gray-600 mt-1">
                            Resolves: {new Date(signal.resolutionDate).toLocaleDateString()}
                          </p>
                        </div>
                        <div className="text-right">
                          <span className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded text-sm font-semibold">
                            {signal.validationCount} validations
                          </span>
                          <Link
                            href={`/signals/${signal.id}`}
                            className="block mt-3 text-blue-600 hover:underline text-sm"
                          >
                            Review & Moderate
                          </Link>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Ready for Resolution */}
            <div>
              <h2 className="text-2xl font-bold mb-4">Ready for Resolution ({readyForResolution.length})</h2>
              {loadingSignals ? (
                <p className={mono.className}>Loading...</p>
              ) : readyForResolution.length === 0 ? (
                <p className="text-gray-500">No signals ready for resolution</p>
              ) : (
                <div className="grid gap-4">
                  {readyForResolution.map((signal) => (
                    <div
                      key={signal.id}
                      className="bg-white border border-green-200 rounded p-4 hover:shadow-md transition-shadow"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <Link
                            href={`/signals/${signal.id}`}
                            className="text-lg font-semibold text-[#1C2541] hover:underline"
                          >
                            {signal.title}
                          </Link>
                          <div className={`${mono.className} text-sm text-gray-600 mt-1`}>
                            by <strong>{signal.submitterUsername}</strong> in <strong>{signal.domainName}</strong>
                          </div>
                          <p className="text-sm text-gray-600 mt-1">
                            Resolves: {new Date(signal.resolutionDate).toLocaleDateString()}
                          </p>
                        </div>
                        <div className="text-right">
                          <span className="bg-green-100 text-green-800 px-3 py-1 rounded text-sm font-semibold">
                            {signal.validationCount} validations
                          </span>
                          <Link
                            href={`/signals/${signal.id}`}
                            className="block mt-3 text-blue-600 hover:underline text-sm"
                          >
                            Resolve Signal
                          </Link>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Promotions Tab */}
        {activeTab === "promotions" && (
          <div>
            <h2 className="text-2xl font-bold mb-4">Pending Promotion Requests</h2>
            {loadingPromotions ? (
              <p className={mono.className}>Loading...</p>
            ) : promotionRequests.length === 0 ? (
              <p className="text-gray-500">No pending promotion requests</p>
            ) : (
              <div className="grid gap-4">
                {promotionRequests.map((req) => (
                  <div
                    key={req.id}
                    className="bg-white border border-[#DEDCD3] rounded p-4"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-semibold">{req.username}</p>
                        <p className="text-sm text-gray-600 mt-1">
                          Requested promotion
                        </p>
                        {req.justification && (
                          <p className="text-sm text-gray-700 mt-2">{req.justification}</p>
                        )}
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleApprovePromotion(req.id)}
                          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleRejectPromotion(req.id)}
                          className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
