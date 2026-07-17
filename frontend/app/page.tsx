"use client";

import { useEffect, useState } from "react";
import api from "@/service/api";

export default function Home() {
  const [status, setStatus] = useState<string>("Checking...");

  useEffect(() => {
    api
      .get("/api/health")
      .then((res) => setStatus(res.data.status))
      .catch(() => setStatus("DOWN"));
  }, []);

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4">
      <h1 className="text-3xl font-bold">Signalroom</h1>
      <p className="text-lg text-gray-500">Public Track Record Platform</p>
      <div className="mt-6 rounded-lg border px-4 py-2">
        Backend Status: <span className="font-semibold">{status}</span>
      </div>
    </main>
    );
}

