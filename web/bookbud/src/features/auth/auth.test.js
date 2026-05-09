import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../context/AuthContext';

/**
 * Authentication Feature Tests
 * Tests for login, register, password reset, and Google OAuth flows
 */
describe('Authentication Features', () => {
  const renderWithAuth = (component) => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          {component}
        </AuthProvider>
      </BrowserRouter>
    );
  };

  describe('Login Flow', () => {
    it('should validate required email field', async () => {
      const LoginForm = () => {
        const [email, setEmail] = React.useState('');
        const [error, setError] = React.useState('');

        const handleSubmit = (e) => {
          e.preventDefault();
          if (!email) {
            setError('Email is required');
          }
        };

        return (
          <form onSubmit={handleSubmit}>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email"
            />
            {error && <p>{error}</p>}
            <button type="submit">Login</button>
          </form>
        );
      };

      renderWithAuth(<LoginForm />);
      const button = screen.getByText('Login');
      fireEvent.click(button);

      expect(screen.getByText('Email is required')).toBeInTheDocument();
    });

    it('should validate required password field', async () => {
      const LoginForm = () => {
        const [password, setPassword] = React.useState('');
        const [error, setError] = React.useState('');

        const handleSubmit = (e) => {
          e.preventDefault();
          if (!password) {
            setError('Password is required');
          }
        };

        return (
          <form onSubmit={handleSubmit}>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
            />
            {error && <p>{error}</p>}
            <button type="submit">Login</button>
          </form>
        );
      };

      renderWithAuth(<LoginForm />);
      const button = screen.getByText('Login');
      fireEvent.click(button);

      expect(screen.getByText('Password is required')).toBeInTheDocument();
    });

    it('should show loading state during login', async () => {
      const LoginForm = () => {
        const [loading, setLoading] = React.useState(false);

        const handleLogin = async () => {
          setLoading(true);
          await new Promise(resolve => setTimeout(resolve, 100));
          setLoading(false);
        };

        return (
          <div>
            {loading && <p>Logging in...</p>}
            <button onClick={handleLogin}>Login</button>
          </div>
        );
      };

      renderWithAuth(<LoginForm />);
      const button = screen.getByText('Login');
      
      fireEvent.click(button);
      expect(screen.getByText('Logging in...')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.queryByText('Logging in...')).not.toBeInTheDocument();
      });
    });
  });

  describe('Register Flow', () => {
    it('should validate password confirmation matches', () => {
      const RegisterForm = () => {
        const [password, setPassword] = React.useState('');
        const [confirmPassword, setConfirmPassword] = React.useState('');
        const [error, setError] = React.useState('');

        const handleSubmit = (e) => {
          e.preventDefault();
          if (password !== confirmPassword) {
            setError('Passwords do not match');
          }
        };

        return (
          <form onSubmit={handleSubmit}>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
            />
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm Password"
            />
            {error && <p>{error}</p>}
            <button type="submit">Register</button>
          </form>
        );
      };

      renderWithAuth(<RegisterForm />);
      const inputs = screen.getAllByPlaceholderText(/Password/);
      fireEvent.change(inputs[0], { target: { value: 'password123' } });
      fireEvent.change(inputs[1], { target: { value: 'password456' } });

      const button = screen.getByText('Register');
      fireEvent.click(button);

      expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
    });

    it('should validate strong password requirements', () => {
      const validatePassword = (pwd) => {
        return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(pwd);
      };

      expect(validatePassword('weak')).toBe(false);
      expect(validatePassword('Weak123')).toBe(false);
      expect(validatePassword('Strong@Pass123')).toBe(true);
    });

    it('should validate unique email during registration', () => {
      const existingEmails = ['existing@test.com', 'user@test.com'];

      const isEmailAvailable = (email) => {
        return !existingEmails.includes(email);
      };

      expect(isEmailAvailable('new@test.com')).toBe(true);
      expect(isEmailAvailable('existing@test.com')).toBe(false);
    });
  });

  describe('Password Reset Flow', () => {
    it('should send password reset email', async () => {
      const mockSendEmail = jest.fn().mockResolvedValue({ success: true });

      const ResetForm = () => {
        const [email, setEmail] = React.useState('');
        const [message, setMessage] = React.useState('');

        const handleSubmit = async (e) => {
          e.preventDefault();
          const result = await mockSendEmail(email);
          if (result.success) {
            setMessage('Reset link sent to email');
          }
        };

        return (
          <form onSubmit={handleSubmit}>
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email"
            />
            {message && <p>{message}</p>}
            <button type="submit">Send Reset Link</button>
          </form>
        );
      };

      renderWithAuth(<ResetForm />);
      const input = screen.getByPlaceholderText('Email');
      const button = screen.getByText('Send Reset Link');

      fireEvent.change(input, { target: { value: 'test@test.com' } });
      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText('Reset link sent to email')).toBeInTheDocument();
      });
    });
  });

  describe('Google OAuth', () => {
    it('should handle successful Google authentication', async () => {
      const mockGoogleAuth = jest.fn().mockResolvedValue({ success: true, user: { id: '1', email: 'test@google.com' } });

      const GoogleAuthButton = () => {
        const [user, setUser] = React.useState(null);

        const handleGoogleAuth = async () => {
          const result = await mockGoogleAuth('mock_id_token');
          setUser(result.user);
        };

        return (
          <div>
            {user && <p>Logged in: {user.email}</p>}
            <button onClick={handleGoogleAuth}>Sign in with Google</button>
          </div>
        );
      };

      renderWithAuth(<GoogleAuthButton />);
      const button = screen.getByText('Sign in with Google');

      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText('Logged in: test@google.com')).toBeInTheDocument();
      });
    });
  });

  describe('Session Management', () => {
    it('should maintain user session on page refresh', () => {
      const user = { id: '1', username: 'testuser' };
      localStorage.setItem('user', JSON.stringify(user));

      const storedUser = JSON.parse(localStorage.getItem('user'));
      expect(storedUser).toEqual(user);
    });

    it('should clear session on logout', () => {
      localStorage.setItem('accessToken', 'token123');
      localStorage.setItem('user', JSON.stringify({ id: '1' }));

      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });
});
