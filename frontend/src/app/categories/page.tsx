"use client";

import { useEffect, useState } from "react";
import Protected from "@/components/Protected";
import { apiCategories, apiCreateCategory, apiDeleteCategory, apiUpdateCategory } from "@/lib/api";
import { ApiError, Category } from "@/lib/types";

const inputCls =
  "rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500";

function CategoriesInner() {
  const [cats, setCats] = useState<Category[]>([]);
  const [name, setName] = useState("");
  const [color, setColor] = useState("#6366f1");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  function load() {
    apiCategories().then(setCats).catch(() => {});
  }
  useEffect(() => {
    load();
  }, []);

  function reset() {
    setName("");
    setColor("#6366f1");
    setEditingId(null);
  }

  async function save(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      if (editingId) await apiUpdateCategory(editingId, { name, color });
      else await apiCreateCategory({ name, color });
      reset();
      load();
    } catch (err) {
      setError((err as ApiError).message ?? "Save failed");
    }
  }

  function startEdit(c: Category) {
    setEditingId(c.id);
    setName(c.name);
    setColor(c.color ?? "#6366f1");
  }

  async function del(id: string) {
    if (confirm("Delete this category? Tasks in it stay, just become uncategorised.")) {
      await apiDeleteCategory(id);
      if (editingId === id) reset();
      load();
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-bold text-slate-900">Categories</h1>

      <form onSubmit={save} className="mb-6 flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4">
        <div className="flex-1">
          <label className="mb-1 block text-sm font-medium text-slate-700">Name</label>
          <input className={`${inputCls} w-full`} value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Work" required />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Color</label>
          <input type="color" value={color} onChange={(e) => setColor(e.target.value)} className="h-10 w-14 cursor-pointer rounded-lg border border-slate-300" />
        </div>
        <button type="submit" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700">
          {editingId ? "Update" : "Add"}
        </button>
        {editingId && (
          <button type="button" onClick={reset} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-50">
            Cancel
          </button>
        )}
      </form>
      {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

      {cats.length === 0 ? (
        <p className="rounded-xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-400">No categories yet.</p>
      ) : (
        <ul className="space-y-2">
          {cats.map((c) => (
            <li key={c.id} className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white p-3">
              <span className="h-5 w-5 rounded-full border border-slate-200" style={{ backgroundColor: c.color ?? "#cbd5e1" }} />
              <span className="flex-1 font-medium text-slate-800">{c.name}</span>
              <button onClick={() => startEdit(c)} className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-50">
                Edit
              </button>
              <button onClick={() => del(c.id)} className="rounded-lg border border-rose-200 px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50">
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function CategoriesPage() {
  return (
    <Protected>
      <CategoriesInner />
    </Protected>
  );
}
