// "use client";

// import { useState } from "react";
// import { useRouter } from "next/navigation";
// import Link from "next/link";
// import { loginApi } from "@/service/auth";
// import { useAuth } from "@/context/AuthContext";

// export default function LoginPage() {
//   const router = useRouter();
//   const { login } = useAuth();
//   const [form, setForm] = useState({ email: "", password: "" });
//   const [error, setError] = useState("");
//   const [loading, setLoading] = useState(false);

//   const handleSubmit = async (e: React.FormEvent) => {
//     e.preventDefault();
//     setError("");
//     setLoading(true);
//     try {
//       const data = await loginApi(form);
//       login(data);
//       router.push("/");
//     } catch (err: unknown) {
//       const msg =
//         (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
//         "Invalid email or password";
//       setError(msg);
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <main className="flex min-h-screen items-center justify-center bg-gray-50">
//       <div className="w-full max-w-md rounded-2xl border bg-white p-8 shadow-sm">
//         <h1 className="mb-1 text-2xl font-bold text-gray-900">Sign in</h1>
//         <p className="mb-6 text-sm text-gray-500">Welcome back to Signalroom</p>

//         {error && (
//           <div className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
//             {error}
//           </div>
//         )}

//         <form onSubmit={handleSubmit} className="space-y-4">
//           <div>
//             <label className="mb-1 block text-sm font-medium text-gray-700">Email</label>
//             <input
//               type="email"
//               required
//               value={form.email}
//               onChange={(e) => setForm({ ...form, email: e.target.value })}
//               className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-black focus:ring-1 focus:ring-black"
//               placeholder="you@example.com"
//             />
//           </div>

//           <div>
//             <label className="mb-1 block text-sm font-medium text-gray-700">Password</label>
//             <input
//               type="password"
//               required
//               value={form.password}
//               onChange={(e) => setForm({ ...form, password: e.target.value })}
//               className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-black focus:ring-1 focus:ring-black"
//               placeholder="••••••••"
//             />
//           </div>

//           <button
//             type="submit"
//             disabled={loading}
//             className="w-full rounded-lg bg-black py-2 text-sm font-medium text-white transition hover:bg-gray-800 disabled:opacity-50"
//           >
//             {loading ? "Signing in..." : "Sign in"}
//           </button>
//         </form>

//         <p className="mt-6 text-center text-sm text-gray-500">
//           Don&apos;t have an account?{" "}
//           <Link href="/auth/register" className="font-medium text-black hover:underline">
//             Sign up
//           </Link>
//         </p>
//       </div>
//     </main>
//   );
// }



"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Source_Serif_4, IBM_Plex_Mono } from "next/font/google";
import { loginApi } from "@/service/auth";
import { useAuth } from "@/context/AuthContext";

const serif = Source_Serif_4({ subsets: ["latin"], weight: ["400", "600", "700"] });
const mono = IBM_Plex_Mono({ subsets: ["latin"], weight: ["400", "500"] });

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await loginApi(form);
      login(data);
      router.push("/");
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        "Invalid email or password";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className={`${serif.className} flex min-h-screen flex-col bg-[#F5F5F1] text-[#1C2541]`}>
      {/* Header rule — same register mark as the home page */}
      <header className="flex items-center justify-between border-b border-[#DEDCD3] px-6 py-4 sm:px-10">
        <Link href="/" className="flex items-center gap-2.5">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="9.5" stroke="#1C2541" strokeWidth="1" />
            <path d="M6 12.5 L9.5 9 L12.5 12 L16 7" stroke="#7A2E2E" strokeWidth="1.3" fill="none" />
          </svg>
          <span className="text-[15px] font-semibold tracking-wide">Signalroom</span>
        </Link>
        <span className={`${mono.className} text-[11px] tracking-widest text-[#8A8D85]`}>
          ACCESS REGISTER
        </span>
      </header>

      {/* Body */}
      <div className="flex flex-1 items-center justify-center px-6 py-16">
        <div className="w-full max-w-sm">
          <p className={`${mono.className} mb-3 text-center text-[11px] tracking-[0.25em] text-[#7A2E2E]`}>
            MEMBER SIGN IN
          </p>
          <h1 className="text-center text-3xl font-semibold tracking-tight">Sign In</h1>
          <p className="mt-2 text-center text-[15px] text-[#5B6472]">Welcome back to Signalroom.</p>

          {/* Ledger card */}
          <div className="mt-8 border border-[#DEDCD3] bg-[#FCFBF8] p-6">
            {error && (
              <div
                className={`${mono.className} mb-5 border-l-2 border-[#7A2E2E] bg-[#FBEDEC] px-3 py-2 text-[12px] tracking-wide text-[#7A2E2E]`}
              >
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className={`${mono.className} mb-1.5 block text-[11px] tracking-widest text-[#5B6472]`}>
                  EMAIL
                </label>
                <input
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  className="w-full border-0 border-b border-[#B9B7AC] bg-transparent px-0 py-2 text-[15px] text-[#1C2541] outline-none placeholder:text-[#B9B7AC] focus:border-[#1C2541]"
                  placeholder="you@example.com"
                />
              </div>

              <div>
                <label className={`${mono.className} mb-1.5 block text-[11px] tracking-widest text-[#5B6472]`}>
                  PASSWORD
                </label>
                <input
                  type="password"
                  required
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  className="w-full border-0 border-b border-[#B9B7AC] bg-transparent px-0 py-2 text-[15px] text-[#1C2541] outline-none placeholder:text-[#B9B7AC] focus:border-[#1C2541]"
                  placeholder="••••••••"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full border border-[#1C2541] bg-[#1C2541] py-2.5 text-center text-[13px] font-semibold tracking-wide text-[#F5F5F1] transition-colors hover:bg-[#141B32] disabled:opacity-50"
              >
                {loading ? "Signing in…" : "Sign In"}
              </button>
            </form>
          </div>

          <p className={`${mono.className} mt-6 text-center text-[11px] tracking-wide text-[#5B6472]`}>
            NEW HERE?{" "}
            <Link
              href="/auth/register"
              className="text-[#7A2E2E] underline decoration-dotted underline-offset-4 hover:text-[#1C2541]"
            >
              REGISTER
            </Link>
          </p>
        </div>
      </div>
    </main>
  );
}