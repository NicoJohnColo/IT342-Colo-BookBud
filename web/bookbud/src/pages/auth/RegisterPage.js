import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

const AUTH_FEATURES = [
  { icon: '🚚', title: 'Meet Up Friendly', desc: 'Safe and easy in-person exchange' },
  { icon: '🕐', title: 'Support 24/7', desc: "We're here to help with your transactions" },
  { icon: '🛡️', title: '100% Real Listings', desc: 'Posted by verified BookBud users' },
];

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, loading, error, clearError } = useAuth();

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreed: false,
  });
  const [formErrors, setFormErrors] = useState({});
  const [successMsg, setSuccessMsg] = useState('');

  const validate = () => {
    const errs = {};
    if (!form.username || form.username.length < 3)
      errs.username = 'Username must be at least 3 characters';
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email))
      errs.email = 'Enter a valid email';
    if (!form.password || form.password.length < 8)
      errs.password = 'Password must be at least 8 characters';
    else if (!/(?=.*[A-Z])(?=.*\d)/.test(form.password))
      errs.password = 'Password must contain at least one uppercase letter and one digit';
    if (form.password !== form.confirmPassword)
      errs.confirmPassword = 'Passwords do not match';
    if (!form.agreed) errs.agreed = 'You must agree to the terms';
    return errs;
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    if (formErrors[name]) setFormErrors((prev) => ({ ...prev, [name]: '' }));
    if (error) clearError();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setFormErrors(errs); return; }
    try {
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        confirmPassword: form.confirmPassword,
      });
      setSuccessMsg('Account created! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch {
      /* error handled by context */
    }
  };

  return (
    <div className="auth-page">
      <Navbar />
      <main className="auth-main">
        <div className="auth-bg" />
        <div className="auth-card">
          <h2>Create Account</h2>
          <p className="auth-subtitle">Join BookBud and start reading for less</p>

          {error && <div className="auth-alert error">{error}</div>}
          {successMsg && <div className="auth-alert success">{successMsg}</div>}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                id="username"
                type="text"
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="Username"
                className={formErrors.username ? 'input-error' : ''}
              />
              {formErrors.username && <span className="input-error-msg">{formErrors.username}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="test1@gmail.com"
                className={formErrors.email ? 'input-error' : ''}
              />
              {formErrors.email && <span className="input-error-msg">{formErrors.email}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="••••••••••••••"
                className={formErrors.password ? 'input-error' : ''}
              />
              {formErrors.password && <span className="input-error-msg">{formErrors.password}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                id="confirmPassword"
                type="password"
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={handleChange}
                placeholder="••••••••••••••"
                className={formErrors.confirmPassword ? 'input-error' : ''}
              />
              {formErrors.confirmPassword && (
                <span className="input-error-msg">{formErrors.confirmPassword}</span>
              )}
            </div>

            <div className="checkbox-group">
              <input
                id="agreed"
                type="checkbox"
                name="agreed"
                checked={form.agreed}
                onChange={handleChange}
              />
              <label htmlFor="agreed">
                By signing up you agree to our{' '}
                <Link to="#!">Terms</Link>,{' '}
                <Link to="#!">Privacy Policy</Link>, and{' '}
                <Link to="#!">Cookie Use</Link>
              </label>
            </div>
            {formErrors.agreed && <span className="input-error-msg" style={{marginBottom:'10px',display:'block'}}>{formErrors.agreed}</span>}

            <button type="submit" className="btn-auth" disabled={loading}>
              {loading ? 'Creating account...' : <>CREATE &nbsp;→</>}
            </button>
          </form>

          <p className="auth-footer-text">
            Already have an account?&nbsp;<Link to="/login">Sign in</Link>
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

export default RegisterPage;
