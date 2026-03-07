import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const isActive = (hash) => location.hash === hash || (hash === '' && location.pathname === '/' && !location.hash);

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          <span className="logo-bb">B</span><span className="logo-b2">B</span>
        </Link>

        <button className="navbar-hamburger" onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu">
          <span /><span /><span />
        </button>

        <ul className={`navbar-links ${menuOpen ? 'open' : ''}`}>
          <li><a href="/#home" className={isActive('#home') || isActive('') ? 'active' : ''} onClick={() => setMenuOpen(false)}>Home</a></li>
          <li><a href="/#about" className={isActive('#about') ? 'active' : ''} onClick={() => setMenuOpen(false)}>About</a></li>
          <li><a href="/#genres" className={isActive('#genres') ? 'active' : ''} onClick={() => setMenuOpen(false)}>Genres</a></li>
          <li><a href="/#books" className={isActive('#books') ? 'active' : ''} onClick={() => setMenuOpen(false)}>Books</a></li>
          <li><a href="/#gallery" className={isActive('#gallery') ? 'active' : ''} onClick={() => setMenuOpen(false)}>Gallery</a></li>
          {user && <li><Link to="/dashboard" onClick={() => setMenuOpen(false)}>Dashboard</Link></li>}
        </ul>

        <div className="navbar-auth">
          {user ? (
            <div className="navbar-user-menu">
              <span className="navbar-username">Hi, {user.username}</span>
              <button className="btn-signin" onClick={handleLogout}>Sign Out</button>
            </div>
          ) : (
            <Link to="/login" className="btn-signin">Sign in</Link>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
