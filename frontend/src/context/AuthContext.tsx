import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role, AuthResponse } from '../types';
import { authApi } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  role: Role | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { email: string; password: string }) => Promise<AuthResponse>;
  register: (userData: any) => Promise<AuthResponse>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const cached = localStorage.getItem('annadhata_user');
    if (!cached) return null;
    try {
      return JSON.parse(cached);
    } catch {
      localStorage.removeItem('annadhata_user');
      return null;
    }
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('annadhata_token');
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const role = user?.role || null;
  const isAuthenticated = !!token && !!user;

  useEffect(() => {
    if (token) {
      refreshUser().finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, [token]);

  const refreshUser = async () => {
    try {
      const res = await authApi.me();
      if (res.success && res.data) {
        setUser(res.data);
        localStorage.setItem('annadhata_user', JSON.stringify(res.data));
      }
    } catch (err: any) {
      console.error('Failed to refresh user profile:', err);
      if (err?.response?.status === 401 || err?.response?.status === 403) {
        logout();
      }
    }
  };

  const login = async (credentials: { email: string; password: string }): Promise<AuthResponse> => {
    const res = await authApi.login(credentials);
    if (res.success && res.data) {
      const authData = res.data;
      setToken(authData.token);
      localStorage.setItem('annadhata_token', authData.token);

      const minimalUser: User = {
        id: authData.id,
        name: authData.name,
        email: authData.email,
        role: authData.role,
        organizationName: authData.organizationName,
        organizationType: authData.organizationType,
        verificationStatus: authData.verificationStatus,
        latitude: authData.latitude,
        longitude: authData.longitude,
        address: authData.address,
        active: true,
      };

      setUser(minimalUser);
      localStorage.setItem('annadhata_user', JSON.stringify(minimalUser));
      return authData;
    }
    throw new Error(res.message || 'Login failed');
  };

  const register = async (userData: any): Promise<AuthResponse> => {
    const res = await authApi.register(userData);
    if (res.success && res.data) {
      const authData = res.data;
      setToken(authData.token);
      localStorage.setItem('annadhata_token', authData.token);

      const minimalUser: User = {
        id: authData.id,
        name: authData.name,
        email: authData.email,
        role: authData.role,
        organizationName: authData.organizationName,
        organizationType: authData.organizationType,
        verificationStatus: authData.verificationStatus,
        latitude: authData.latitude,
        longitude: authData.longitude,
        address: authData.address,
        active: true,
      };

      setUser(minimalUser);
      localStorage.setItem('annadhata_user', JSON.stringify(minimalUser));
      return authData;
    }
    throw new Error(res.message || 'Registration failed');
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('annadhata_token');
    localStorage.removeItem('annadhata_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role,
        isAuthenticated,
        isLoading,
        login,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
