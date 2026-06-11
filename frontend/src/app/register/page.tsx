"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { ApiError } from "@/lib/types";

export default function RegisterPage() {
  const { register } = useAuth();
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [details, setDetails] = useState<Record<string, string> | undefined>();
  const [busy, setBusy] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setDetails(undefined);
    setBusy(true);
    try {
      await register(email, password, displayName);
    } catch (err) {
      const apiErr = err as ApiError;
      setError(apiErr.message ?? "Registration failed");
      setDetails(apiErr.details); // field-level messages from the backend (Phase 9)
    } finally {
      setBusy(false);
    }
  }

  const field = (label: string, value: string, set: (v: string) => void, type = "text", key?: string) => (
    <div>
      <label className="mb-1 block text-sm font-medium text-slate-700">{label}</label>
      <input
        type={type}
        value={value}
        onChange={(e) => set(e.target.value)}
        className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        required
      />
      {key && details?.[key] && <p className="mt-1 text-xs text-red-600">{details[key]}</p>}
    </div>
  );

  return (
    <div className="mx-auto flex min-h-[calc(100vh-3.5rem)] max-w-md flex-col justify-center px-4">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-6 text-center">
          <div className="text-3xl font-bold text-indigo-600">📋 TaskFlow</div>
          <p className="mt-1 text-sm text-slate-500">Create your account</p>
        </div>
        <form onSubmit={onSubmit} className="space-y-4">
          {field("Display name", displayName, setDisplayName, "text", "displayName")}
          {field("Email", email, setEmail, "email", "email")}
          {field("Password", password, setPassword, "password", "password")}
          {error && <p className="text-sm text-red-600">{error}</p>}
          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-lg bg-indigo-600 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-60"
          >
            {busy ? "Creating…" : "Create account"}
          </button>
        </form>
        <p className="mt-4 text-center text-sm text-slate-500">
          Already have an account?{" "}
          <Link href="/login" className="font-medium text-indigo-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
