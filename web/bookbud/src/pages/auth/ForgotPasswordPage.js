import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

const AUTH_FEATURES = [
  { icon: '🚚', title: 'Meet Up Friendly', desc: 'Safe and easy in-person exchange' },
  { icon: '🕐', title: 'Support 24/7', desc: "We're here to help with your transactions" },
  { icon: '🛡️', title: '100% Real Listings', desc: 'Posted by verified BookBud users' },
];

const ForgotPasswordPage = () => {
  const { forgotPassword, loading, error, clearError } = useAuth();
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleChange = (e) => {
    setEmail(e.target.value);
    if (emailError) setEmailError('');
    if (error) clearError();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !/\S+@\S+\.\S+/.test(email)) {
      setEmailError('Enter a valid email address');
      return;
    }
    try {
      await forgotPassword({ email });
      setSuccessMsg('A password reset link has been sent to your email address.');
    } catch {
      /* error handled by context */
    }
  };

  return (
    <div className="auth-page">
      <Navbar />
      <main className="auth-main">
        <div className="auth-bg" />
        <div className="auth-card" style={{ maxWidth: 380 }}>
          <h2>Forgot Password?</h2>
          <p className="auth-subtitle">Enter your email and we'll send you a reset link</p>

          {error && <div className="auth-alert error">{error}</div>}
          {successMsg && <div className="auth-alert success">{successMsg}</div>}

          {!successMsg && (
            <form onSubmit={handleSubmit} noValidate>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  name="email"
                  value={email}
                  onChange={handleChange}
                  placeholder="test1@gmail.com"
                  className={emailError ? 'input-error' : ''}
                />
                {emailError && <span className="input-error-msg">{emailError}</span>}
              </div>

              <button type="submit" className="btn-auth" disabled={loading}>
                {loading ? 'Sending...' : <>SEND &nbsp;→</>}
              </button>
            </form>
          )}

          <p className="auth-footer-text">
            Back to Login&nbsp;<Link to="/login">Sign in</Link>
          </p>
        </div>
      </main>

      <div className="auth-features">
        {AUTH_FEATURES.map((f) => (
          <div key={f.title} className="auth-feature-item">
            <span className="feat-icon">{f.icon}</span>
            <div>
              <strong>{f.title}</strong>
              <p>{f.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
