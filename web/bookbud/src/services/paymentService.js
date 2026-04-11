import api from './api';

const paymentService = {
  // Get all payments for the current user (as payer and receiver)
  getMyPayments: async (params = {}) => {
    try {
      const response = await api.get('/api/v1/payments', { params });
      return response.data?.data || response.data || [];
    } catch (error) {
      console.error('Error fetching payments:', error);
      throw error;
    }
  },

  // Get single payment details
  getPaymentById: async (paymentId) => {
    try {
      const response = await api.get(`/api/v1/payments/${paymentId}`);
      return response.data?.data || response.data;
    } catch (error) {
      console.error('Error fetching payment details:', error);
      throw error;
    }
  },

  // Get payments received by current user (earnings)
  getPaymentsReceived: async (params = {}) => {
    try {
      const response = await api.get('/api/v1/payments/received', { params });
      return response.data?.data || response.data || [];
    } catch (error) {
      console.error('Error fetching received payments:', error);
      throw error;
    }
  },

  // Get payments made by current user (spending)
  getPaymentsMade: async (params = {}) => {
    try {
      const response = await api.get('/api/v1/payments/made', { params });
      return response.data?.data || response.data || [];
    } catch (error) {
      console.error('Error fetching made payments:', error);
      throw error;
    }
  },

  // Get total earnings summary
  getEarningsSummary: async () => {
    try {
      const response = await api.get('/api/v1/earnings/summary');
      return response.data?.data || response.data || {};
    } catch (error) {
      console.error('Error fetching earnings summary:', error);
      throw error;
    }
  },

  // Update payment status (mark as received, etc.)
  updatePaymentStatus: async (paymentId, status) => {
    try {
      const response = await api.put(`/api/v1/payments/${paymentId}/status`, { status });
      return response.data?.data || response.data;
    } catch (error) {
      console.error('Error updating payment status:', error);
      throw error;
    }
  },

  // Create payment for a transaction
  createPayment: async (transactionId, paymentData) => {
    try {
      const response = await api.post(`/api/v1/payments`, {
        transactionId,
        ...paymentData,
      });
      return response.data?.data || response.data;
    } catch (error) {
      console.error('Error creating payment:', error);
      throw error;
    }
  },

  // Get payment statistics (for dashboard)
  getPaymentStats: async () => {
    try {
      const response = await api.get('/api/v1/payments/stats');
      return response.data?.data || response.data || {};
    } catch (error) {
      console.error('Error fetching payment stats:', error);
      return {
        totalEarnings: 0,
        pendingPayments: 0,
        successfulPayments: 0,
        failedPayments: 0,
      };
    }
  },

  // Get earnings by date range
  getEarningsByDateRange: async (startDate, endDate) => {
    try {
      const response = await api.get('/api/v1/earnings/range', {
        params: { startDate, endDate },
      });
      return response.data?.data || response.data || [];
    } catch (error) {
      console.error('Error fetching earnings by date range:', error);
      throw error;
    }
  },
};

export default paymentService;
