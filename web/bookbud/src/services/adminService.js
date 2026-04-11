import api from './api';

const adminService = {
  // Books Management
  getBooks: async (params = {}) => {
    const response = await api.get('/admin/books', { params });
    return response.data?.data;
  },

  updateBookStatus: async (bookId, status) => {
    const response = await api.put(`/admin/books/${bookId}/status`, status, {
      headers: { 'Content-Type': 'application/json' }
    });
    return response.data?.data;
  },

  deleteBook: async (bookId) => {
    const response = await api.delete(`/admin/books/${bookId}`);
    return response.data?.data;
  },

  // Users Management
  getUsers: async (params = {}) => {
    const response = await api.get('/admin/users', { params });
    return response.data?.data;
  },

  updateUserStatus: async (userId, status) => {
    const response = await api.put(`/admin/users/${userId}/status`, status, {
      headers: { 'Content-Type': 'application/json' }
    });
    return response.data?.data;
  },

  // Transactions Management
  getTransactions: async (params = {}) => {
    const response = await api.get('/admin/transactions', { params });
    return response.data?.data;
  },

  cancelTransaction: async (transactionId) => {
    const response = await api.put(`/admin/transactions/${transactionId}/cancel`);
    return response.data?.data;
  },

  // Notifications
  getNotifications: async (params = {}) => {
    const response = await api.get('/admin/notifications', { params });
    return response.data?.data;
  },
};

export default adminService;
