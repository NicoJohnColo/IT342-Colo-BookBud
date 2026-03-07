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

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, loading, error, clearError } = useAuth();

  const [form, setForm] = useState({ email: '', password: '' });
  const [formErrors, setFormErrors] = useState({});
  const [successMsg, setSuccessMsg] = useState('');

  const validate = () => {
    const errs = {};
    if (!form.email) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Enter a valid email';
    if (!form.password) errs.password = 'Password is required';
    return errs;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors((prev) => ({ ...prev, [name]: '' }));
    if (error) clearError();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setFormErrors(errs); return; }
    try {
      await login({ email: form.email, password: form.password });
      setSuccessMsg('Login successful! Redirecting...');
      setTimeout(() => navigate('/dashboard'), 1000);
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
          <h2>Welcome Back</h2>
          <p className="auth-subtitle">Sign in to your BookBud account</p>

          {error && <div className="auth-alert error">{error}</div>}
          {successMsg && <div className="auth-alert success">{successMsg}</div>}

          <form onSubmit={handleSubmit} noValidate>
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
              <div className="form-row-between">
                <label htmlFor="password">Password</label>
                <Link to="/forgot-password">Forgot Password?</Link>
              </div>
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

            <button type="submit" className="btn-auth" disabled={loading}>
              {loading ? 'Signing in...' : <>SIGN IN &nbsp;→</>}
            </button>
          </form>

          <p className="auth-footer-text">
            I don't have an account ?&nbsp;<Link to="/register">Sign up</Link>
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

export default LoginPage;
