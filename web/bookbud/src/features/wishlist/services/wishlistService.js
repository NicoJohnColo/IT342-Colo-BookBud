import api from './api';

const wishlistService = {
  getMyWishlist: async () => {
    const response = await api.get('/wishlist');
    return response.data?.data || [];
  },

  addToWishlist: async (bookId) => {
    const response = await api.post('/wishlist', { bookId });
    return response.data?.data;
  },

  removeFromWishlist: async (wishlistId) => {
    const response = await api.delete(`/wishlist/${wishlistId}`);
    return response.data?.data;
  },
};

export default wishlistService;
