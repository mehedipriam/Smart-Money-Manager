import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react';
import * as authService from '../services/authService.js';
import * as userService from '../services/userService.js';
import { clearSession, getSession, setSession, setStoredUser } from '../utils/tokenStorage.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    const session = getSession();
    if (session?.user) {
      setUser(session.user);
    }
    setIsBootstrapping(false);
  }, []);

  useEffect(() => {
    function handleExpired() {
      setUser(null);
    }
    window.addEventListener('smm:auth-expired', handleExpired);
    return () => window.removeEventListener('smm:auth-expired', handleExpired);
  }, []);

  const login = useCallback(async (credentials) => {
    const result = await authService.login(credentials);
    setSession({ accessToken: result.accessToken, refreshToken: result.refreshToken, user: result.user });
    setUser(result.user);
    return result.user;
  }, []);

  const register = useCallback(async (payload) => authService.register(payload), []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearSession();
      setUser(null);
    }
  }, []);

  const refreshProfile = useCallback(async () => {
    const profile = await userService.getProfile();
    setStoredUser(profile);
    setUser(profile);
    return profile;
  }, []);

  const updateLocalUser = useCallback((profile) => {
    setStoredUser(profile);
    setUser(profile);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      isBootstrapping,
      login,
      register,
      logout,
      refreshProfile,
      updateLocalUser,
    }),
    [user, isBootstrapping, login, register, logout, refreshProfile, updateLocalUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
