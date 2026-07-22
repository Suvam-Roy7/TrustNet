import { createContext, useContext, useMemo, useState } from "react";

import { loginRequest, logoutRequest, registerRequest } from "../api/authApi";

import tokenService from "./tokenService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    tokenService.hasAccessToken(),
  );

  const login = async (credentials) => {
    const tokens = await loginRequest(credentials);

    tokenService.setTokens(tokens);

    setIsAuthenticated(true);

    return tokens;
  };

  const register = async (registrationData) => {
    return registerRequest(registrationData);
  };

  const logout = async () => {
    const refreshToken = tokenService.getRefreshToken();

    try {
      if (refreshToken) {
        await logoutRequest(refreshToken);
      }
    } finally {
      tokenService.clearTokens();
      setIsAuthenticated(false);
    }
  };

  const value = useMemo(
    () => ({
      isAuthenticated,
      login,
      register,
      logout,
    }),
    [isAuthenticated],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
