"use client";

import { useCallback, useEffect, useState } from "react";
import Protected from "@/components/Protected";
import {
  apiCategories,
  apiCreateTask,
  apiDeleteTask,
  apiTasks,
  apiToggleComplete,
  apiUpdateTask,
  TaskBody,
} from "@/lib/api";
import { ApiError, Category, PageResponse, Priority, Task, TaskStatus } from "@/lib/types";

const STATUSES: TaskStatus[] = ["TODO", "IN_PROGRESS", "DONE"];
const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH"];

const statusBadge: Record<TaskStatus, string> = {
  TODO: "bg-slate-100 text-slate-700",
  IN_PROGRESS: "bg-amber-100 text-amber-700",
  DONE: "bg-emerald-100 text-emerald-700",
};
const priorityBadge: Record<Priority, string> = {
  LOW: "bg-slate-100 text-slate-600",
  MEDIUM: "bg-violet-100 text-violet-700",
  HIGH: "bg-rose-100 text-rose-700",
};

const inputCls =
  "w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500";

function TasksInner() {
  const [page, setPage] = useState<PageResponse<Task> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [filters, setFilters] = useState({
    status: "" as TaskStatus | "",
    priority: "" as Priority | "",
    categoryId: "",
    search: "",
  });
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Task | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setPage(await apiTasks({ ...filters, page: pageNum, size: 8 }));
    } finally {
      setLoading(false);
    }
  }, [filters, pageNum]);

  useEffect(() => {
    apiCategories().then(setCategories).catch(() => {});
  }, []);
  useEffect(() => {
    load();
  }, [load]);

  function setFilter<K extends keyof typeof filters>(k: K, v: (typeof filters)[K]) {
    setPageNum(0);
    setFilters((f) => ({ ...f, [k]: v }));
  }

  async function toggle(id: string) {
    await apiToggleComplete(id);
    load();
  }
  async function del(id: string) {
    if (confirm("Delete this task?")) {
      await apiDeleteTask(id);
      load();
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Tasks</h1>
        <button
          onClick={() => {
            setEditing(null);
            setModalOpen(true);
          }}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
        >
          + New task
        </button>
      </div>

      {/* Filters */}
      <div className="mb-5 grid grid-cols-2 gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:grid-cols-4">
        <select className={inputCls} value={filters.status} onChange={(e) => setFilter("status", e.target.value as TaskStatus | "")}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <select className={inputCls} value={filters.priority} onChange={(e) => setFilter("priority", e.target.value as Priority | "")}>
          <option value="">All priorities</option>
          {PRIORITIES.map((p) => (
            <option key={p} value={p}>{p}</option>
          ))}
        </select>
        <select className={inputCls} value={filters.categoryId} onChange={(e) => setFilter("categoryId", e.target.value)}>
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <input className={inputCls} placeholder="Search…" value={filters.search} onChange={(e) => setFilter("search", e.target.value)} />
      </div>

      {/* List */}
      {loading ? (
        <p className="text-slate-400">Loading…</p>
      ) : !page || page.content.length === 0 ? (
        <p className="rounded-xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-400">
          No tasks found. Try adjusting filters or add a new task.
        </p>
      ) : (
        <ul className="space-y-3">
          {page.content.map((t) => (
            <li key={t.id} className="flex items-center gap-4 rounded-xl border border-slate-200 bg-white p-4">
              <div className="flex-1">
                <p className={`font-medium ${t.status === "DONE" ? "text-slate-400 line-through" : "text-slate-900"}`}>{t.title}</p>
                <div className="mt-1.5 flex flex-wrap items-center gap-2 text-xs">
                  <span className={`rounded-full px-2 py-0.5 font-semibold ${statusBadge[t.status]}`}>{t.status}</span>
                  <span className={`rounded-full px-2 py-0.5 font-semibold ${priorityBadge[t.priority]}`}>{t.priority}</span>
                  {t.category && (
                    <span className="rounded-full bg-cyan-100 px-2 py-0.5 font-semibold text-cyan-700">{t.category.name}</span>
                  )}
                  {t.dueDate && <span className="text-slate-400">due {new Date(t.dueDate).toLocaleDateString()}</span>}
                </div>
              </div>
              <button
                onClick={() => toggle(t.id)}
                className={`rounded-lg px-3 py-1.5 text-xs font-semibold ${
                  t.status === "DONE" ? "border border-slate-200 text-slate-600 hover:bg-slate-50" : "bg-emerald-600 text-white hover:bg-emerald-700"
                }`}
              >
                {t.status === "DONE" ? "Undo" : "Done"}
              </button>
              <button onClick={() => { setEditing(t); setModalOpen(true); }} className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-50">
                Edit
              </button>
              <button onClick={() => del(t.id)} className="rounded-lg border border-rose-200 px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50">
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}

      {/* Pagination */}
      {page && page.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-4 text-sm">
          <button disabled={page.first} onClick={() => setPageNum((n) => n - 1)} className="rounded-lg border border-slate-200 px-3 py-1.5 disabled:opacity-40">
            ← Prev
          </button>
          <span className="text-slate-500">Page {page.page + 1} of {page.totalPages}</span>
          <button disabled={page.last} onClick={() => setPageNum((n) => n + 1)} className="rounded-lg border border-slate-200 px-3 py-1.5 disabled:opacity-40">
            Next →
          </button>
        </div>
      )}

      {modalOpen && (
        <TaskModal
          task={editing}
          categories={categories}
          onClose={() => setModalOpen(false)}
          onSaved={() => {
            setModalOpen(false);
            load();
          }}
        />
      )}
    </div>
  );
}

function TaskModal({
  task,
  categories,
  onClose,
  onSaved,
}: {
  task: Task | null;
  categories: Category[];
  onClose: () => void;
  onSaved: () => void;
}) {
  const [title, setTitle] = useState(task?.title ?? "");
  const [description, setDescription] = useState(task?.description ?? "");
  const [status, setStatus] = useState<TaskStatus>(task?.status ?? "TODO");
  const [priority, setPriority] = useState<Priority>(task?.priority ?? "MEDIUM");
  const [categoryId, setCategoryId] = useState(task?.category?.id ?? "");
  const [dueDate, setDueDate] = useState(task?.dueDate ? task.dueDate.slice(0, 10) : "");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    const body: TaskBody = {
      title,
      description: description || null,
      status,
      priority,
      dueDate: dueDate ? new Date(dueDate).toISOString() : null,
      categoryId: categoryId || null,
    };
    try {
      if (task) await apiUpdateTask(task.id, body);
      else await apiCreateTask(body);
      onSaved();
    } catch (err) {
      const e2 = err as ApiError;
      setError(e2.details ? Object.values(e2.details).join(", ") : e2.message ?? "Save failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}>
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
        <h2 className="mb-4 text-lg font-bold text-slate-900">{task ? "Edit task" : "New task"}</h2>
        <form onSubmit={save} className="space-y-3">
          <input className={inputCls} placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
          <textarea className={inputCls} placeholder="Description (optional)" value={description} onChange={(e) => setDescription(e.target.value)} rows={2} />
          <div className="grid grid-cols-2 gap-3">
            <select className={inputCls} value={status} onChange={(e) => setStatus(e.target.value as TaskStatus)}>
              {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <select className={inputCls} value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
            <select className={inputCls} value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">No category</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <input type="date" className={inputCls} value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={onClose} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-50">
              Cancel
            </button>
            <button type="submit" disabled={busy} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-60">
              {busy ? "Saving…" : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function TasksPage() {
  return (
    <Protected>
      <TasksInner />
    </Protected>
  );
}
