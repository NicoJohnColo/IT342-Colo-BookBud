import api from './api';

const userService = {
  getUserProfile: async (userId) => {
    const response = await api.get(`/users/${userId}`);
    return response.data?.data;
  },

  updateUserProfile: async (userId, payload) => {
    const response = await api.put(`/users/${userId}`, payload);
    return response.data?.data;
  },
};

export default userService;
