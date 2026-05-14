import bookClient from '../api/bookClient';

const bookService = {
  getAllBooks: async (params = {}) => {
    const response = await bookClient.get('/books', { params });
    return response.data;
  },

  getBookById: async (bookId) => {
    const response = await bookClient.get(`/books/${bookId}`);
    return response.data;
  },

  createBook: async (bookData) => {
    const response = await bookClient.post('/books', bookData);
    return response.data;
  },

  uploadBookImage: async (bookId, imageFile) => {
    const formData = new FormData();
    formData.append('image', imageFile);

    const response = await bookClient.post(`/books/${bookId}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  updateBook: async (bookId, bookData) => {
    const response = await bookClient.put(`/books/${bookId}`, bookData);
    return response.data;
  },

  deleteBook: async (bookId) => {
    const response = await bookClient.delete(`/books/${bookId}`);
    return response.data;
  },

  searchExternalBooks: async (q) => {
    const response = await bookClient.get('/books/search-external', { params: { q } });
    return response.data?.data || response.data || [];
  },
};

export default bookService;



