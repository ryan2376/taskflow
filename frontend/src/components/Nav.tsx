"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth";

// Top navigation bar. Renders nothing until the user is logged in (so it stays
// hidden on the login/register screens).
export default function Nav() {
  const { user, token, logout } = useAuth();
  const pathname = usePathname();

  if (!token) return null;

  const NavLink = ({ href, label }: { href: string; label: string }) => (
    <Link
      href={href}
      className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
        pathname === href
          ? "bg-indigo-600 text-white"
          : "text-slate-600 hover:bg-slate-100"
      }`}
    >
      {label}
    </Link>
  );

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto max-w-5xl px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <span className="text-lg font-bold text-indigo-600">📋 TaskFlow</span>
          <nav className="flex gap-1">
            <NavLink href="/tasks" label="Tasks" />
            <NavLink href="/categories" label="Categories" />
            <NavLink href="/analytics" label="Analytics" />
          </nav>
        </div>
        <div className="flex items-center gap-3">
          <span className="hidden sm:inline text-sm text-slate-500">{user?.displayName}</span>
          <button
            onClick={logout}
            className="text-sm rounded-lg border border-slate-200 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}
