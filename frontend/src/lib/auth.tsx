"use client";

// Auth state for the whole app, shared via React context. Holds the current user +
// token, restores the session from localStorage on first load, and exposes
// login/register/logout. Any client component can call useAuth().

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { useRouter } from "next/navigation";
import { User } from "./types";
import { apiLogin, apiMe, apiRegister, clearToken, getToken, setToken } from "./api";

interface AuthContextValue {
  user: User | null;
  token: string | null;
  ready: boolean; // false until we've checked localStorage on first load
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setTok] = useState<string | null>(null);
  const [ready, setReady] = useState(false);
  const router = useRouter();

  // On first load: if we have a saved token, verify it by fetching the profile.
  useEffect(() => {
    const saved = getToken();
    if (saved) {
      setTok(saved);
      apiMe()
        .then(setUser)
        .catch(() => {
          clearToken();
          setTok(null);
        })
        .finally(() => setReady(true));
    } else {
      setReady(true);
    }
  }, []);

  async function login(email: string, password: string) {
    const res = await apiLogin({ email, password });
    setToken(res.token);
    setTok(res.token);
    setUser(res.user);
    router.push("/tasks");
  }

  async function register(email: string, password: string, displayName: string) {
    const res = await apiRegister({ email, password, displayName });
    setToken(res.token);
    setTok(res.token);
    setUser(res.user);
    router.push("/tasks");
  }

  function logout() {
    clearToken();
    setTok(null);
    setUser(null);
    router.push("/login");
  }

  return (
    <AuthContext.Provider value={{ user, token, ready, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
