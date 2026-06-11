// A tiny typed client for the TaskFlow API. Every call goes through `request`,
// which attaches the JWT, sets JSON headers, and turns the backend's ErrorResponse
// into a thrown ApiError the UI can catch.

import {
  AnalyticsSummary,
  ApiError,
  AuthResponse,
  Category,
  PageResponse,
  Priority,
  Task,
  TaskStatus,
  User,
} from "./types";

const BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";
const TOKEN_KEY = "taskflow_token";

// ---- token storage (localStorage; client-side only) ----
export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    ...((options.headers as Record<string, string>) ?? {}),
  };
  if (options.body) headers["Content-Type"] = "application/json";
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE}${path}`, { ...options, headers });

  if (res.status === 204) return undefined as T; // No Content (e.g. DELETE)

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const err: ApiError = {
      status: res.status,
      message: data?.message ?? res.statusText,
      details: data?.details,
    };
    throw err;
  }
  return data as T;
}

// ---- auth ----
export const apiRegister = (body: { email: string; password: string; displayName: string }) =>
  request<AuthResponse>("/auth/register", { method: "POST", body: JSON.stringify(body) });

export const apiLogin = (body: { email: string; password: string }) =>
  request<AuthResponse>("/auth/login", { method: "POST", body: JSON.stringify(body) });

export const apiMe = () => request<User>("/users/me");

// ---- categories ----
export const apiCategories = () => request<Category[]>("/categories");
export const apiCreateCategory = (body: { name: string; color?: string | null }) =>
  request<Category>("/categories", { method: "POST", body: JSON.stringify(body) });
export const apiUpdateCategory = (id: string, body: { name: string; color?: string | null }) =>
  request<Category>(`/categories/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const apiDeleteCategory = (id: string) =>
  request<void>(`/categories/${id}`, { method: "DELETE" });

// ---- tasks ----
export interface TaskFilters {
  status?: TaskStatus | "";
  priority?: Priority | "";
  categoryId?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export function apiTasks(f: TaskFilters): Promise<PageResponse<Task>> {
  const p = new URLSearchParams();
  if (f.status) p.set("status", f.status);
  if (f.priority) p.set("priority", f.priority);
  if (f.categoryId) p.set("categoryId", f.categoryId);
  if (f.search) p.set("search", f.search);
  p.set("page", String(f.page ?? 0));
  p.set("size", String(f.size ?? 10));
  p.set("sort", f.sort ?? "createdAt,desc");
  return request<PageResponse<Task>>(`/tasks?${p.toString()}`);
}

export interface TaskBody {
  title: string;
  description?: string | null;
  status?: TaskStatus;
  priority?: Priority;
  dueDate?: string | null;
  categoryId?: string | null;
}

export const apiCreateTask = (body: TaskBody) =>
  request<Task>("/tasks", { method: "POST", body: JSON.stringify(body) });
export const apiUpdateTask = (id: string, body: TaskBody) =>
  request<Task>(`/tasks/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const apiDeleteTask = (id: string) =>
  request<void>(`/tasks/${id}`, { method: "DELETE" });
export const apiToggleComplete = (id: string) =>
  request<Task>(`/tasks/${id}/complete`, { method: "PATCH" });

// ---- analytics ----
export const apiAnalytics = () => request<AnalyticsSummary>("/analytics/summary");
