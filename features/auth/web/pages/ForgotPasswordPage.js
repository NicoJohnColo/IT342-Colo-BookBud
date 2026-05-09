import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import Navbar from '../../components/Navbar/Navbar';
import AuthCard from '../../components/AuthCard/AuthCard';
import FormInput from '../../components/FormInput/FormInput';
import { validateEmail } from '../../utils/validators';
import styles from './ForgotPasswordPage.module.css';

const ForgotPasswordPage = () => {
  const { handleForgotPassword, isLoading, error, clearError } = useAuth();

  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [showError, setShowError] = useState(false);

  useEffect(() => {
    if (error) setShowError(true);
  }, [error]);

  const dismissError = () => {
    setShowError(false);
    clearError();
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    const result = validateEmail(email);
    if (!result.valid) {
      setEmailError(result.message);
      return;
    }
    const response = await handleForgotPassword(email);
    if (!response.error) {
      setSubmitted(true);
    }
  };

  return (
    <div className={styles.page}>
      <Navbar />
      <main className={styles.main}>
        <AuthCard>
          {!submitted ? (
            <>
              <h2 className={styles.heading}>Forgot Password?</h2>
              <p className={styles.subtitle}>
                Enter your email and we&apos;ll send you a reset link.
              </p>

              {showError && error && (
                <div className={styles.errorBanner}>
                  <span>{error}</span>
                  <button onClick={dismissError} className={styles.dismissButton} aria-label="Dismiss error">
                    ×
                  </button>
                </div>
              )}

              <form onSubmit={onSubmit} noValidate>
                <FormInput
                  id="forgot-email"
                  label="Email"
                  type="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (emailError) setEmailError('');
                    if (error) clearError();
                  }}
                  error={emailError}
                  placeholder="you@example.com"
                />

                <button type="submit" className={styles.submitButton} disabled={isLoading} aria-label="Send reset link">
                  {isLoading ? (
                    <span className={styles.spinner} />
                  ) : (
                    'Send Reset Link'
                  )}
                </button>
              </form>
            </>
          ) : (
            <div className={styles.successState}>
              <div className={styles.checkIcon}>
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#22C55E" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <p className={styles.successMessage}>
                We&apos;ve sent a password reset link to <strong>{email}</strong>. Please check your inbox.
              </p>
            </div>
          )}

          <Link to="/login" className={styles.backLink}>
            ← Back to Sign In
          </Link>
        </AuthCard>
      </main>
    </div>
  );
};

export default ForgotPasswordPage;
