import api from './api';

const transactionService = {
  createTransaction: async (payload) => {
    const response = await api.post('/transactions', payload);
    return response.data?.data;
  },

  getMyTransactions: async (params = {}) => {
    const response = await api.get('/transactions', { params });
    return response.data?.data;
  },

  updateTransactionStatus: async (transactionId, status) => {
    const response = await api.put(`/transactions/${transactionId}/status`, { status });
    return response.data?.data;
  },

  submitRating: async (transactionId, rating) => {
    const response = await api.post(`/transactions/${transactionId}/rating`, { rating });
    return response.data?.data;
  },
};

export default transactionService;
