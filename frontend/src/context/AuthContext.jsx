import { createContext, useContext, useState, useRef, useCallback, useMemo } from 'react';
import { setAxiosToken } from '../api/axiosInstance';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('rentease_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    const savedToken = localStorage.getItem('rentease_token');
    if (savedToken) {
      setAxiosToken(savedToken);
    }
    return savedToken || null;
  });

  const tokenRef = useRef(token);

  const login = useCallback((authResponse) => {
    // authResponse = { token: '...', user: { id, firstName, lastName, email, role, verificationStatus, ... } }
    const { token: jwt, user: userData } = authResponse;
    setToken(jwt);
    tokenRef.current = jwt;
    setAxiosToken(jwt);
    setUser(userData);
    localStorage.setItem('rentease_token', jwt);
    localStorage.setItem('rentease_user', JSON.stringify(userData));
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    tokenRef.current = null;
    setAxiosToken(null);
    setUser(null);
    localStorage.removeItem('rentease_token');
    localStorage.removeItem('rentease_user');
  }, []);

  const isAuthenticated = !!token;
  const role = user?.role ?? null;

  const hasRole = useCallback(
    (allowedRoles) => {
      if (!role) return false;
      return allowedRoles.includes(role);
    },
    [role]
  );

  const updateUser = useCallback((userData) => {
    setUser((prev) => {
      const updated = { ...prev, ...userData };
      localStorage.setItem('rentease_user', JSON.stringify(updated));
      return updated;
    });
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      tokenRef,
      isAuthenticated,
      role,
      login,
      logout,
      updateUser,
      hasRole,
    }),
    [user, token, isAuthenticated, role, login, logout, updateUser, hasRole]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}

export default AuthContext;
