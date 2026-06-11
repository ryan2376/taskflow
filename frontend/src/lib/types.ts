// TypeScript mirrors of the API's JSON shapes (DTOs). Keeping them here gives us
// end-to-end type safety when calling the backend.

export type TaskStatus = "TODO" | "IN_PROGRESS" | "DONE";
export type Priority = "LOW" | "MEDIUM" | "HIGH";

export interface User {
  id: string;
  email: string;
  displayName: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface Category {
  id: string;
  name: string;
  color: string | null;
}

export interface Task {
  id: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: Priority;
  dueDate: string | null;
  completedAt: string | null;
  category: Category | null;
  createdAt: string;
  updatedAt: string;
}

// Matches the backend's PageResponse<T> (Phase 8).
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// Matches AnalyticsSummary (Phase 10).
export interface AnalyticsSummary {
  total: number;
  completed: number;
  pending: number;
  overdue: number;
  completionRate: number;
  byPriority: Record<string, number>;
  byCategory: Record<string, number>;
}

// Matches the backend's ErrorResponse (Phase 9).
export interface ApiError {
  status: number;
  message: string;
  details?: Record<string, string>;
}
