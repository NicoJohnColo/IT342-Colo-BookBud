import { useAuth as useAuthContext } from '../context/AuthContext';

// Wrapper hook to provide a stable API for components
// that expect handleLogout, handleForgotPassword, and isLoading.
const useAuth = () => {
  const {
    user,
    loading,
    error,
    register,
    login,
    googleAuth,
    logout,
    forgotPassword,
    clearError,
    isAuthenticated,
  } = useAuthContext();

  const handleLogout = async () => {
    await logout();
  };

  const handleForgotPassword = async (email) => {
    // AuthContext expects an object; components pass just the email.
    return forgotPassword({ email });
  };

  return {
    user,
    isLoading: loading,
    error,
    register,
    login,
    googleAuth,
    handleLogout,
    handleForgotPassword,
    clearError,
    isAuthenticated,
  };
};

export default useAuth;
