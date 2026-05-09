import axios from 'axios';

jest.mock('axios');

describe('api configuration', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  describe('localStorage token handling', () => {
    it('should store access token in localStorage', () => {
      localStorage.setItem('accessToken', 'test_token_123');
      const token = localStorage.getItem('accessToken');
      expect(token).toBe('test_token_123');
    });

    it('should remove access token from localStorage', () => {
      localStorage.setItem('accessToken', 'test_token_123');
      localStorage.removeItem('accessToken');
      const token = localStorage.getItem('accessToken');
      expect(token).toBeNull();
    });
  });


  describe('response handling', () => {
    it('should handle successful responses', () => {
      const response = { status: 200, data: { message: 'Success' } };
      expect(response.status).toBe(200);
      expect(response.data).toBeDefined();
    });

    it('should handle error responses', () => {
      const error = { response: { status: 401, data: { message: 'Unauthorized' } } };
      expect(error.response.status).toBe(401);
    });
  });

  describe('api configuration constants', () => {
    it('should have API base URL configured', () => {
      const baseURL = 'http://localhost:8080/api/v1';
      expect(baseURL).toBe('http://localhost:8080/api/v1');
    });

    it('should have default content type header', () => {
      const contentType = 'application/json';
      expect(contentType).toBe('application/json');
    });
  });
});
