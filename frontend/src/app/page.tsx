"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

// Entry point: send the user to their tasks if logged in, otherwise to login.
export default function Home() {
  const { ready, token } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (ready) router.replace(token ? "/tasks" : "/login");
  }, [ready, token, router]);

  return <div className="mx-auto max-w-5xl px-4 py-16 text-slate-400">Loading…</div>;
}
