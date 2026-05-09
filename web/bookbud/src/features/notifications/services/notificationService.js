import api from './api';

const notificationService = {
  getMyNotifications: async () => {
    const response = await api.get('/notifications');
    return response.data?.data || [];
  },

  markAsRead: async (notificationId) => {
    const response = await api.put(`/notifications/${notificationId}/read`);
    return response.data?.data;
  },

  markAllAsRead: async () => {
    const response = await api.put('/notifications/read-all');
    return response.data?.data;
  },

  deleteNotification: async (notificationId) => {
    const response = await api.delete(`/notifications/${notificationId}`);
    return response.data?.data;
  },
};

export default notificationService;
