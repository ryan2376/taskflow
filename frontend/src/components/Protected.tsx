"use client";

// Wrap any page that requires a logged-in user. While we're still checking the
// stored token it shows a loader; if there's no token it redirects to /login.

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export default function Protected({ children }: { children: React.ReactNode }) {
  const { token, ready } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (ready && !token) router.replace("/login");
  }, [ready, token, router]);

  if (!ready || !token) {
    return <div className="mx-auto max-w-5xl px-4 py-16 text-slate-400">Loading…</div>;
  }
  return <>{children}</>;
}
