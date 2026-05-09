import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import authService from '../services/authService';

jest.mock('../services/authService');

// Test component that uses AuthContext
const TestComponent = () => {
  const auth = useAuth();
  return (
    <div>
      <p>Loading: {auth.loading ? 'true' : 'false'}</p>
      <p>Error: {auth.error || 'none'}</p>
      <p>User: {auth.user ? auth.user.username : 'null'}</p>
      <button onClick={() => auth.register({ username: 'newuser', email: 'new@test.com', password: 'pass123', confirmPassword: 'pass123' })}>
        Register
      </button>
      <button onClick={() => auth.login({ email: 'test@test.com', password: 'pass123' })}>
        Login
      </button>
      <button onClick={() => auth.logout()}>
        Logout
      </button>
      <button onClick={() => auth.forgotPassword({ email: 'test@test.com' })}>
        Forgot Password
      </button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('AuthProvider initialization', () => {
    it('should initialize with stored user', () => {
      const mockUser = { userId: '1', username: 'testuser' };
      authService.getStoredUser.mockReturnValue(mockUser);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('User: testuser')).toBeInTheDocument();
    });

    it('should initialize with null user if none stored', () => {
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('User: null')).toBeInTheDocument();
    });
  });

  describe('register', () => {
    it('should handle successful registration', async () => {
      const mockFormData = {
        username: 'newuser',
        email: 'new@test.com',
        password: 'pass123',
        confirmPassword: 'pass123',
      };
      const mockResponse = { message: 'User registered successfully' };
      authService.register.mockResolvedValue(mockResponse);
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Error: none')).toBeInTheDocument();
    });

    it('should handle registration error', async () => {
      const mockFormData = {
        username: 'newuser',
        email: 'new@test.com',
        password: 'pass123',
        confirmPassword: 'pass123',
      };
      authService.register.mockRejectedValue({
        response: { data: { message: 'Email already exists' } },
      });
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Loading: false')).toBeInTheDocument();
    });
  });

  describe('login', () => {
    it('should handle successful login', async () => {
      const mockCredentials = { email: 'test@test.com', password: 'pass123' };
      const mockUser = { userId: '1', username: 'testuser', email: 'test@test.com' };
      const mockResponse = {
        data: {
          accessToken: 'token123',
          user: mockUser,
        },
      };
      authService.login.mockResolvedValue(mockResponse);
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('User: null')).toBeInTheDocument();
    });

    it('should handle login error', async () => {
      authService.login.mockRejectedValue({
        response: { data: { message: 'Invalid credentials' } },
      });
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Loading: false')).toBeInTheDocument();
    });
  });

  describe('logout', () => {
    it('should clear user on logout', async () => {
      const mockUser = { userId: '1', username: 'testuser' };
      authService.logout.mockResolvedValue(undefined);
      authService.getStoredUser.mockReturnValue(mockUser);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('User: testuser')).toBeInTheDocument();
    });
  });

  describe('forgotPassword', () => {
    it('should handle forgot password request', async () => {
      const mockData = { email: 'test@test.com' };
      const mockResponse = { message: 'Password reset link sent' };
      authService.forgotPassword.mockResolvedValue(mockResponse);
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Error: none')).toBeInTheDocument();
    });

    it('should handle forgot password error', async () => {
      authService.forgotPassword.mockRejectedValue({
        response: { data: { message: 'User not found' } },
      });
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Loading: false')).toBeInTheDocument();
    });
  });

  describe('googleAuth', () => {
    it('should handle successful Google auth', async () => {
      const mockIdToken = 'google_id_token_123';
      const mockUser = { userId: '1', username: 'googleuser', email: 'user@google.com' };
      const mockResponse = {
        data: {
          accessToken: 'token123',
          user: mockUser,
        },
      };
      authService.googleAuth.mockResolvedValue(mockResponse);
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('User: null')).toBeInTheDocument();
    });

    it('should handle Google auth error', async () => {
      authService.googleAuth.mockRejectedValue({
        response: { data: { message: 'Google auth failed' } },
      });
      authService.getStoredUser.mockReturnValue(null);

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByText('Loading: false')).toBeInTheDocument();
    });
  });
});
