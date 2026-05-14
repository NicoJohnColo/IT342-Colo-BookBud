import authClient from '../api/authClient';

const authService = {
  register: async ({ username, email, password, confirmPassword }) => {
    const response = await authClient.post('/auth/register', {
      username,
      email,
      password,
      confirmPassword,
    });
    return response.data;
  },

  login: async ({ email, password }) => {
    const response = await authClient.post('/auth/login', { email, password });
    const { data } = response.data;
    if (data) {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
    }
    return response.data;
  },

  googleAuth: async ({ idToken }) => {
    const response = await authClient.post('/auth/google', { idToken });
    const { data } = response.data;
    if (data) {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
    }
    return response.data;
  },

  forgotPassword: async ({ email }) => {
    const response = await authClient.post('/auth/forgot-password', { email });
    return response.data;
  },

  logout: async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    try {
      await authClient.post('/auth/logout', { refreshToken });
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  refreshToken: async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await authClient.post('/auth/refresh', { refreshToken });
    const { data } = response.data;
    if (data) {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
    }
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await authClient.get('/auth/me');
    return response.data;
  },

  getStoredUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('accessToken');
  },
};

export default authService;



