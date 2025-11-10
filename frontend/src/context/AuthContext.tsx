import React, { createContext, useContext, useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";
import { GoogleLogin } from "@react-oauth/google";


interface GooglePayload {
  sub: string;
  iat?: string;
  exp?: string;
}

interface AuthContextType {
  user: GooglePayload | null;
  token: string | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<GooglePayload | null>(null);
  const [token, setToken] = useState<string | null>(null);

  // ✅ 로그인 시 토큰 저장 및 유저 상태 갱신
  const login = (token: string) => {
    localStorage.setItem("token", token);
    setToken(token);
    const decoded = jwtDecode<GooglePayload>(token);
    setUser(decoded);
  };

  // ✅ 로그아웃 시 상태 및 로컬스토리지 초기화
  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  };

  // ✅ 새로고침 시 JWT 복원
  useEffect(() => {
    const savedToken = localStorage.getItem("token");
    if (savedToken) {
      try {
        const decoded = jwtDecode<GooglePayload>(savedToken);
        setToken(savedToken);
        setUser(decoded);
      } catch {
        localStorage.removeItem("token");
      }
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {user ? (
        children
      ) : (
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            height: "100vh",
          }}
        >
          <h2>🎁 마니또에 로그인하기</h2>
          <GoogleLogin
            onSuccess={(res) => login(res.credential!)}
            onError={() => alert("로그인 실패 😢")}
          />
        </div>
      )}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
};