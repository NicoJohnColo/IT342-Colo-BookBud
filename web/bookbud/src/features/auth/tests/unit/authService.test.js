import authService from './authService';
import api from './api';

jest.mock('./api');

describe('authService', () => {
  afterEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('register', () => {
    it('should call POST /auth/register with credentials', async () => {
      const formData = {
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
        confirmPassword: 'password123',
      };
      const mockResponse = { data: { message: 'User registered successfully' } };
      api.post.mockResolvedValue(mockResponse);

      const result = await authService.register(formData);

      expect(api.post).toHaveBeenCalledWith('/auth/register', formData);
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle registration errors', async () => {
      const formData = {
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
        confirmPassword: 'password123',
      };
      api.post.mockRejectedValue(new Error('Registration failed'));

      await expect(authService.register(formData)).rejects.toThrow('Registration failed');
    });
  });

  describe('login', () => {
    it('should save tokens and user to localStorage on successful login', async () => {
      const credentials = { email: 'test@example.com', password: 'password123' };
      const mockUser = { userId: '1', username: 'testuser', email: 'test@example.com' };
      const mockResponse = {
        data: {
          data: {
            accessToken: 'access_token_123',
            refreshToken: 'refresh_token_123',
            user: mockUser,
          },
        },
      };
      api.post.mockResolvedValue(mockResponse);

      const result = await authService.login(credentials);

      expect(api.post).toHaveBeenCalledWith('/auth/login', credentials);
      expect(localStorage.getItem('accessToken')).toBe('access_token_123');
      expect(localStorage.getItem('refreshToken')).toBe('refresh_token_123');
      expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login errors', async () => {
      const credentials = { email: 'test@example.com', password: 'wrong' };
      api.post.mockRejectedValue(new Error('Invalid credentials'));

      await expect(authService.login(credentials)).rejects.toThrow('Invalid credentials');
    });
  });

  describe('googleAuth', () => {
    it('should save tokens and user to localStorage on successful Google auth', async () => {
      const idToken = 'google_id_token_123';
      const mockUser = { userId: '1', username: 'testuser', email: 'test@google.com' };
      const mockResponse = {
        data: {
          data: {
            accessToken: 'access_token_123',
            refreshToken: 'refresh_token_123',
            user: mockUser,
          },
        },
      };
      api.post.mockResolvedValue(mockResponse);

      const result = await authService.googleAuth({ idToken });

      expect(api.post).toHaveBeenCalledWith('/auth/google', { idToken });
      expect(localStorage.getItem('accessToken')).toBe('access_token_123');
      expect(localStorage.getItem('refreshToken')).toBe('refresh_token_123');
      expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('forgotPassword', () => {
    it('should call POST /auth/forgot-password with email', async () => {
      const data = { email: 'test@example.com' };
      const mockResponse = { data: { message: 'Password reset link sent' } };
      api.post.mockResolvedValue(mockResponse);

      const result = await authService.forgotPassword(data);

      expect(api.post).toHaveBeenCalledWith('/auth/forgot-password', data);
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('logout', () => {
    it('should clear localStorage after logout', async () => {
      localStorage.setItem('accessToken', 'token');
      localStorage.setItem('refreshToken', 'refresh');
      localStorage.setItem('user', JSON.stringify({ userId: '1' }));
      api.post.mockResolvedValue({});

      await authService.logout();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });

    it('should clear localStorage even if logout API call fails', async () => {
      localStorage.setItem('accessToken', 'token');
      localStorage.setItem('refreshToken', 'refresh');
      localStorage.setItem('user', JSON.stringify({ userId: '1' }));
      // Mock to silently fail without throwing
      api.post.mockImplementation(() => {
        return Promise.reject(new Error('Logout failed'));
      });

      // The logout function should still clear local storage
      await authService.logout().catch(() => {});

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('refreshToken', () => {
    it('should update tokens in localStorage', async () => {
      localStorage.setItem('refreshToken', 'old_refresh_token');
      const mockUser = { userId: '1', username: 'testuser' };
      const mockResponse = {
        data: {
          data: {
            accessToken: 'new_access_token',
            refreshToken: 'new_refresh_token',
            user: mockUser,
          },
        },
      };
      api.post.mockResolvedValue(mockResponse);

      const result = await authService.refreshToken();

      expect(api.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'old_refresh_token' });
      expect(localStorage.getItem('accessToken')).toBe('new_access_token');
      expect(localStorage.getItem('refreshToken')).toBe('new_refresh_token');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getCurrentUser', () => {
    it('should call GET /auth/me', async () => {
      const mockUser = { userId: '1', username: 'testuser' };
      const mockResponse = { data: mockUser };
      api.get.mockResolvedValue(mockResponse);

      const result = await authService.getCurrentUser();

      expect(api.get).toHaveBeenCalledWith('/auth/me');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getStoredUser', () => {
    it('should return parsed user from localStorage', () => {
      const mockUser = { userId: '1', username: 'testuser' };
      localStorage.setItem('user', JSON.stringify(mockUser));

      const result = authService.getStoredUser();

      expect(result).toEqual(mockUser);
    });

    it('should return null if no user in localStorage', () => {
      const result = authService.getStoredUser();

      expect(result).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true if accessToken exists', () => {
      localStorage.setItem('accessToken', 'token123');

      const result = authService.isAuthenticated();

      expect(result).toBe(true);
    });

    it('should return false if accessToken does not exist', () => {
      const result = authService.isAuthenticated();

      expect(result).toBe(false);
    });
  });
});
