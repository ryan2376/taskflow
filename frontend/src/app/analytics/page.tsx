"use client";

import { useEffect, useState } from "react";
import Protected from "@/components/Protected";
import { apiAnalytics } from "@/lib/api";
import { AnalyticsSummary } from "@/lib/types";

const priorityColor: Record<string, string> = {
  LOW: "#94a3b8",
  MEDIUM: "#8b5cf6",
  HIGH: "#f43f5e",
};

function StatCard({ label, value, accent }: { label: string; value: number | string; accent: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-bold ${accent}`}>{value}</p>
    </div>
  );
}

function BarList({ title, data, colorFor }: { title: string; data: Record<string, number>; colorFor?: (k: string) => string }) {
  const entries = Object.entries(data);
  const max = Math.max(1, ...entries.map(([, v]) => v));
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5">
      <h3 className="mb-4 font-semibold text-slate-800">{title}</h3>
      {entries.length === 0 ? (
        <p className="text-sm text-slate-400">No data.</p>
      ) : (
        <ul className="space-y-3">
          {entries.map(([k, v]) => (
            <li key={k}>
              <div className="mb-1 flex justify-between text-sm">
                <span className="font-medium text-slate-700">{k}</span>
                <span className="text-slate-500">{v}</span>
              </div>
              <div className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100">
                <div
                  className="h-full rounded-full"
                  style={{ width: `${(v / max) * 100}%`, backgroundColor: colorFor ? colorFor(k) : "#6366f1" }}
                />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function AnalyticsInner() {
  const [s, setS] = useState<AnalyticsSummary | null>(null);

  useEffect(() => {
    apiAnalytics().then(setS).catch(() => {});
  }, []);

  if (!s) return <div className="mx-auto max-w-5xl px-4 py-8 text-slate-400">Loading…</div>;

  const pct = Math.round(s.completionRate * 100);

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">Analytics</h1>

      <div className="mb-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard label="Total tasks" value={s.total} accent="text-slate-900" />
        <StatCard label="Completed" value={s.completed} accent="text-emerald-600" />
        <StatCard label="Pending" value={s.pending} accent="text-amber-600" />
        <StatCard label="Overdue" value={s.overdue} accent="text-rose-600" />
      </div>

      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-6">
        <div className="mb-2 flex items-end justify-between">
          <h3 className="font-semibold text-slate-800">Completion rate</h3>
          <span className="text-2xl font-bold text-indigo-600">{pct}%</span>
        </div>
        <div className="h-3 w-full overflow-hidden rounded-full bg-slate-100">
          <div className="h-full rounded-full bg-indigo-600 transition-all" style={{ width: `${pct}%` }} />
        </div>
        <p className="mt-2 text-sm text-slate-500">
          {s.completed} of {s.total} tasks completed
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <BarList title="By priority" data={s.byPriority} colorFor={(k) => priorityColor[k] ?? "#6366f1"} />
        <BarList title="By category" data={s.byCategory} />
      </div>
    </div>
  );
}

export default function AnalyticsPage() {
  return (
    <Protected>
      <AnalyticsInner />
    </Protected>
  );
}
