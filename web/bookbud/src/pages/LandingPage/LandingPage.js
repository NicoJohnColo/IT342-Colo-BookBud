import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar/Navbar';
import Footer from '../../shared/components/Footer/Footer';
import { useAuth } from '../../shared/context/AuthContext';
import GoogleAuthButton from '../../shared/components/GoogleAuthButton/GoogleAuthButton';
import './LandingPage.css';

import { validateEmail } from '../../shared/utils/validators';

// Local image assets
import LandingBg from '../../shared/components/imgs/landingBg.png';

import About1 from '../../shared/components/imgs/About1.png';
import About2 from '../../shared/components/imgs/About2.png';
import About3 from '../../shared/components/imgs/About3.png';
import BuildReadImg from '../../shared/components/imgs/BuildReadImg.png';

import BothDieImg from '../../shared/components/imgs/bothdie.png';
import HarryImg from '../../shared/components/imgs/harry.png';
import NeverImg from '../../shared/components/imgs/never.png';

import ComicImg from '../../shared/components/imgs/comic.png';
import FictionImg from '../../shared/components/imgs/fiction.png';
import MangaImg from '../../shared/components/imgs/manga.png';

import Genre1 from '../../shared/components/imgs/genre1.png';
import Genre2 from '../../shared/components/imgs/genre2.png';
import Genre3 from '../../shared/components/imgs/genre3.png';
import Genre4 from '../../shared/components/imgs/genre4.png';

const GENRES = [
  {
    name: 'Manga',
    img: MangaImg,
  },
  {
    name: 'Comics',
    img: ComicImg,
  },
  {
    name: 'Fiction',
    img: FictionImg,
  },
];

const SAMPLE_BOOKS = [
  {
    bookId: '1',
    title: "Harry Potter and the Philosopher's Stone",
    author: 'J.K. Rowling',
    condition: 'New',
    rentPrice: 45,
    salePrice: 300,
    coverImage: HarryImg,
  },
  {
    bookId: '2',
    title: 'Neverwhere',
    author: 'Neil Gaiman',
    condition: 'Good',
    rentPrice: 40,
    salePrice: 280,
    coverImage: NeverImg,
  },
  {
    bookId: '3',
    title: 'They Both Die at the End',
    author: 'Adam Silvera',
    condition: 'New',
    rentPrice: 45,
    salePrice: 350,
    coverImage: BothDieImg,
  },
];

const GALLERY = [Genre1, Genre2, Genre3, Genre4];

const LandingPage = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState(SAMPLE_BOOKS);
  const [showLogin, setShowLogin] = useState(false);
  const [authMode, setAuthMode] = useState('login'); // 'login' | 'register' | 'forgot'

  const { login, register, googleAuth, forgotPassword, loading, error, clearError } = useAuth();
  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [loginErrors, setLoginErrors] = useState({});

  const [registerForm, setRegisterForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreed: false,
  });
  const [registerErrors, setRegisterErrors] = useState({});

  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotError, setForgotError] = useState('');
  const [forgotSubmitted, setForgotSubmitted] = useState(false);

  const [authSuccess, setAuthSuccess] = useState('');

  const openLogin = () => {
    setAuthMode('login');
    setShowLogin(true);
    setAuthSuccess('');
  };

  const openRegister = () => {
    setAuthMode('register');
    setShowLogin(true);
    setAuthSuccess('');
  };

  const openForgot = () => {
    setAuthMode('forgot');
    setShowLogin(true);
    setAuthSuccess('');
    setForgotSubmitted(false);
  };

  const closeLogin = () => {
    setShowLogin(false);
    setLoginErrors({});
    setLoginForm({ email: '', password: '' });
    setRegisterErrors({});
    setRegisterForm({ username: '', email: '', password: '', confirmPassword: '', agreed: false });
    setForgotEmail('');
    setForgotError('');
    setForgotSubmitted(false);
    setAuthSuccess('');
    if (error) clearError();
  };

  const validateLogin = () => {
    const errs = {};
    if (!loginForm.email) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(loginForm.email)) errs.email = 'Enter a valid email';
    if (!loginForm.password) errs.password = 'Password is required';
    return errs;
  };

  const handleLoginChange = (e) => {
    const { name, value } = e.target;
    setLoginForm((prev) => ({ ...prev, [name]: value }));
    if (loginErrors[name]) setLoginErrors((prev) => ({ ...prev, [name]: '' }));
    if (error) clearError();
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    const errs = validateLogin();
    if (Object.keys(errs).length) { setLoginErrors(errs); return; }
    try {
      await login({ email: loginForm.email, password: loginForm.password });
      setAuthSuccess('Login successful! Redirecting...');
      setTimeout(() => {
        setShowLogin(false);
        navigate('/dashboard');
      }, 800);
    } catch {
      /* error handled by context */
    }
  };

  const validateRegister = () => {
    const errs = {};
    if (!registerForm.username || registerForm.username.length < 3)
      errs.username = 'Username must be at least 3 characters';
    if (!registerForm.email || !/\S+@\S+\.\S+/.test(registerForm.email))
      errs.email = 'Enter a valid email';
    if (!registerForm.password || registerForm.password.length < 8)
      errs.password = 'Password must be at least 8 characters';
    else if (!/(?=.*[A-Z])(?=.*\d)/.test(registerForm.password))
      errs.password = 'Password must contain at least one uppercase letter and one digit';
    if (registerForm.password !== registerForm.confirmPassword)
      errs.confirmPassword = 'Passwords do not match';
    if (!registerForm.agreed) errs.agreed = 'You must agree to the terms';
    return errs;
  };

  const handleRegisterChange = (e) => {
    const { name, value, type, checked } = e.target;
    setRegisterForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    if (registerErrors[name]) setRegisterErrors((prev) => ({ ...prev, [name]: '' }));
    if (error) clearError();
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    const errs = validateRegister();
    if (Object.keys(errs).length) { setRegisterErrors(errs); return; }
    try {
      await register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
      });
      setAuthSuccess('Account created! You can now sign in.');
      setAuthMode('login');
      setRegisterErrors({});
    } catch {
      /* error handled by context */
    }
  };

  const handleGoogleSuccess = async (idToken) => {
    try {
      await googleAuth({ idToken });
      setAuthSuccess('Google sign-in successful! Redirecting...');
      setShowLogin(false);
      setTimeout(() => navigate('/dashboard'), 1000);
    } catch {
      /* handled by context */
    }
  };

  const handleForgotSubmit = async (e) => {
    e.preventDefault();
    const result = validateEmail(forgotEmail);
    if (!result.valid) {
      setForgotError(result.message);
      return;
    }
    try {
      await forgotPassword({ email: forgotEmail });
      setForgotSubmitted(true);
      setForgotError('');
      setAuthSuccess('');
    } catch {
      /* error handled by context */
    }
  };

  return (
    <div className="landing">
      <Navbar onSignInClick={openLogin} />

      {/* ===== HERO ===== */}
      <section id="home" className="hero">
        <div className="hero-bg" style={{ backgroundImage: `url(${LandingBg})` }} />
        <div className="hero-card">
          <p className="hero-eyebrow">Discover New Listings</p>
          <h1 className="hero-title">Find Your Next<br />Favorite Book</h1>
          <p className="hero-desc">
            Browse affordable books for rent or sale listed by real readers near you. Save money, read more.
          </p>
          <a href="#books" className="btn-primary">BROWSE NOW</a>
        </div>
      </section>

      {/* ===== FEATURES BAR ===== */}
      <div className="features-bar">
        <div className="feature-item">
          <span className="feature-icon">🚚</span>
          <div>
            <strong>Meet Up Friendly</strong>
            <p>Safe and easy in-person exchange</p>
          </div>
        </div>
        <div className="feature-item">
          <span className="feature-icon">🕐</span>
          <div>
            <strong>Support 24/7</strong>
            <p>We're here to help with your transactions</p>
          </div>
        </div>
        <div className="feature-item">
          <span className="feature-icon">🛡️</span>
          <div>
            <strong>100% Real Listings</strong>
            <p>Posted by verified BookBud users</p>
          </div>
        </div>
      </div>

      {/* ===== ABOUT ===== */}
      <section id="about" className="section about-section">
        <div className="section-header">
          <h2>About</h2>
          <p>Build Your Reading List Without Breaking the Bank</p>
        </div>

        <div className="about-images">
          <img src={About1} alt="Readers exchanging books" />
          <img src={About2} alt="Stack of books" />
          <img src={About3} alt="Readers enjoying books" />
        </div>

        <div className="about-blurb">
          <div className="about-text">
            <h3>Build Your Reading List<br />Without Breaking the Bank</h3>
            <p>
              BookBud is a peer-to-peer book marketplace built by readers for readers. Rent a book for a few days
              or buy one outright from a fellow reader. Earn from the books sitting on your shelf.
            </p>
            <a href="#!" className="btn-outline">LEARN MORE</a>
          </div>
          <div className="about-img-wrap">
            <div className="about-img-circle" />
            <img
              src={BuildReadImg}
              alt="Build your reading list"
              className="about-img"
            />
          </div>
        </div>
      </section>

      {/* ===== GENRES ===== */}
      <section id="genres" className="section genres-section">
        <div className="section-header">
          <h2>Genres</h2>
          <p>Whatever you love to read, we have it.</p>
        </div>
        <div className="genres-grid">
          {GENRES.map((g) => (
            <div key={g.name} className="genre-card">
              <img src={g.img} alt={g.name} />
              <p>{g.name}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ===== BOOKS ===== */}
      <section id="books" className="section books-section">
        <div className="section-header">
          <h2>Books</h2>
          <p>Trending books listed by our community this week.</p>
        </div>
        <div className="books-grid">
          {books.map((book) => (
            <div key={book.bookId} className="book-card">
              <div className="book-cover">
                <img
                  src={book.coverImage || 'https://covers.openlibrary.org/b/id/260-M.jpg'}
                  alt={book.title}
                  onError={(e) => { e.target.src = 'https://covers.openlibrary.org/b/id/260-M.jpg'; }}
                />
              </div>
              <div className="book-info">
                <h4>{book.title}</h4>
                <p className="book-author">Author: {book.author}</p>
                <p className="book-condition">
                  <span className="badge">{book.condition}</span>
                </p>
                {book.priceRent && <p className="book-price">Rent: ₱{book.priceRent}/week</p>}
                {book.priceSale && <p className="book-price">Sale: ₱{book.priceSale}</p>}
                <div className="book-actions">
                  <button className="btn-rent" onClick={openLogin}>Rent</button>
                  <button className="btn-buy" onClick={openLogin}>Buy</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ===== GALLERY ===== */}
      <section id="gallery" className="section gallery-section">
        <div className="section-header">
          <h2>Gallery</h2>
          <p>See what our community is reading and listing right now.</p>
        </div>
        <div className="gallery-grid">
          {GALLERY.map((src, i) => (
            <div key={i} className="gallery-item">
              <img src={src} alt={`Gallery ${i + 1}`} />
            </div>
          ))}
        </div>
      </section>

      {showLogin && (
        <div className="auth-modal-overlay" onClick={closeLogin}>
          <div className="auth-modal-inner" onClick={(e) => e.stopPropagation()}>
            <div className="auth-card">
              <button className="auth-modal-close" onClick={closeLogin} aria-label="Close sign in">
                ×
              </button>
              <h2 className="auth-heading">{authMode === 'login' ? 'Welcome Back' : 'Create Account'}</h2>
              <p className="auth-subtitle">
                {authMode === 'login'
                  ? 'Sign in to your BookBud account'
                  : 'Join BookBud and start reading for less'}
              </p>

              {error && <div className="auth-error-banner">{error}</div>}
              {authSuccess && <div className="auth-alert success">{authSuccess}</div>}

              {authMode === 'login' && (
                <form onSubmit={handleLoginSubmit} noValidate>
                  <div className="form-group">
                    <label htmlFor="lp-email">Email</label>
                    <input
                      id="lp-email"
                      type="email"
                      name="email"
                      value={loginForm.email}
                      onChange={handleLoginChange}
                      placeholder="test1@gmail.com"
                      className={loginErrors.email ? 'input-error' : ''}
                    />
                    {loginErrors.email && <span className="input-error-msg">{loginErrors.email}</span>}
                  </div>

                  <div className="form-group">
                    <div className="form-row-between">
                      <label htmlFor="lp-password">Password</label>
                      <button
                        type="button"
                        className="forgot-link"
                        onClick={openForgot}
                      >
                        Forgot Password?
                      </button>
                    </div>
                    <input
                      id="lp-password"
                      type="password"
                      name="password"
                      value={loginForm.password}
                      onChange={handleLoginChange}
                      placeholder="••••••••••••••"
                      className={loginErrors.password ? 'input-error' : ''}
                    />
                    {loginErrors.password && <span className="input-error-msg">{loginErrors.password}</span>}
                  </div>

                  <button type="submit" className="auth-btn" disabled={loading}>
                    {loading ? <span className="auth-spinner" /> : <><span>Sign in</span><span className="auth-arrow">→</span></>}
                  </button>
                </form>
              )}

              {authMode === 'register' && (
                <form onSubmit={handleRegisterSubmit} noValidate>
                  <div className="form-group">
                    <label htmlFor="lp-username">Username</label>
                    <input
                      id="lp-username"
                      type="text"
                      name="username"
                      value={registerForm.username}
                      onChange={handleRegisterChange}
                      placeholder="Username"
                      className={registerErrors.username ? 'input-error' : ''}
                    />
                    {registerErrors.username && <span className="input-error-msg">{registerErrors.username}</span>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="lp-reg-email">Email</label>
                    <input
                      id="lp-reg-email"
                      type="email"
                      name="email"
                      value={registerForm.email}
                      onChange={handleRegisterChange}
                      placeholder="test1@gmail.com"
                      className={registerErrors.email ? 'input-error' : ''}
                    />
                    {registerErrors.email && <span className="input-error-msg">{registerErrors.email}</span>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="lp-reg-password">Password</label>
                    <input
                      id="lp-reg-password"
                      type="password"
                      name="password"
                      value={registerForm.password}
                      onChange={handleRegisterChange}
                      placeholder="••••••••••••••"
                      className={registerErrors.password ? 'input-error' : ''}
                    />
                    {registerErrors.password && <span className="input-error-msg">{registerErrors.password}</span>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="lp-reg-confirmPassword">Confirm Password</label>
                    <input
                      id="lp-reg-confirmPassword"
                      type="password"
                      name="confirmPassword"
                      value={registerForm.confirmPassword}
                      onChange={handleRegisterChange}
                      placeholder="••••••••••••••"
                      className={registerErrors.confirmPassword ? 'input-error' : ''}
                    />
                    {registerErrors.confirmPassword && (
                      <span className="input-error-msg">{registerErrors.confirmPassword}</span>
                    )}
                  </div>

                  <div className="checkbox-group">
                    <input
                      id="lp-agreed"
                      type="checkbox"
                      name="agreed"
                      checked={registerForm.agreed}
                      onChange={handleRegisterChange}
                    />
                    <label htmlFor="lp-agreed">
                      By signing up you agree to our{' '}
                      <Link to="#!">Terms</Link>,{' '}
                      <Link to="#!">Privacy Policy</Link>, and{' '}
                      <Link to="#!">Cookie Use</Link>
                    </label>
                  </div>
                  {registerErrors.agreed && (
                    <span className="input-error-msg" style={{ marginBottom: '10px', display: 'block' }}>
                      {registerErrors.agreed}
                    </span>
                  )}

                  <button type="submit" className="auth-btn" disabled={loading}>
                    {loading ? 'Creating account...' : <><span>Create</span><span className="auth-arrow">→</span></>}
                  </button>
                </form>
              )}

              {(authMode === 'login' || authMode === 'register') && (
                <>
                  <div className="auth-divider">or</div>
                  <GoogleAuthButton
                    onSuccess={handleGoogleSuccess}
                    text={authMode === 'login' ? 'signin_with' : 'signup_with'}
                    label={authMode === 'login' ? 'Sign in with Google' : 'Sign up with Google'}
                  />
                </>
              )}

              {authMode === 'forgot' && (
                <form onSubmit={handleForgotSubmit} noValidate>
                  {!forgotSubmitted ? (
                    <>
                      <div className="form-group">
                        <label htmlFor="lp-forgot-email">Email</label>
                        <input
                          id="lp-forgot-email"
                          type="email"
                          name="forgotEmail"
                          value={forgotEmail}
                          onChange={(e) => {
                            setForgotEmail(e.target.value);
                            if (forgotError) setForgotError('');
                            if (error) clearError();
                          }}
                          placeholder="you@example.com"
                          className={forgotError ? 'input-error' : ''}
                        />
                        {forgotError && <span className="input-error-msg">{forgotError}</span>}
                      </div>

                      <button type="submit" className="auth-btn" disabled={loading}>
                        {loading ? <span className="auth-spinner" /> : <><span>Send</span><span className="auth-arrow">→</span></>}
                      </button>
                    </>
                  ) : (
                    <div className="success-state">
                      <div className="check-icon">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#22C55E" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                      </div>
                      <p className="success-message">
                        We've sent a password reset link to <strong>{forgotEmail}</strong>. Please check your inbox.
                      </p>
                    </div>
                  )}
                </form>
              )}

              <p className="auth-footer-text">
                {authMode === 'login' && (
                  <>
                    I don't have an account ?&nbsp;
                    <button
                      className="auth-link"
                      type="button"
                      onClick={() => { setAuthMode('register'); setAuthSuccess(''); }}
                    >
                      Sign up
                    </button>
                  </>
                )}
                {authMode === 'register' && (
                  <>
                    Already have an account?&nbsp;
                    <button
                      className="auth-link"
                      type="button"
                      onClick={() => { setAuthMode('login'); setAuthSuccess(''); }}
                    >
                      Sign in
                    </button>
                  </>
                )}
                {authMode === 'forgot' && (
                  <>
                    Back to Login&nbsp;
                    <button
                      className="auth-link"
                      type="button"
                      onClick={() => { setAuthMode('login'); setAuthSuccess(''); setForgotSubmitted(false); }}
                    >
                      Sign in
                    </button>
                  </>
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
};

export default LandingPage;
