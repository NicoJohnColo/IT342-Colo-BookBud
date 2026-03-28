import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import styles from './Navbar.module.css';
import LogoImg from '../imgs/logo.png';

const NAV_ITEMS = [
  { label: 'Home', href: '/#home' },
  { label: 'About', href: '/#about' },
  { label: 'Genres', href: '/#genres' },
  { label: 'Books', href: '/#books' },
  { label: 'Gallery', href: '/#gallery' },
];

const Navbar = ({ onSignInClick }) => {
  const { user, isAuthenticated, handleLogout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 10);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const onClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  const onLogout = async () => {
    setDropdownOpen(false);
    await handleLogout();
    navigate('/');
  };

  const isActiveHash = (hash) => {
    const target = hash.replace('/#', '#');
    return location.hash === target || (target === '#home' && location.pathname === '/' && !location.hash);
  };

  const userInitial = user?.username
    ? user.username.charAt(0).toUpperCase()
    : user?.name
      ? user.name.charAt(0).toUpperCase()
      : 'U';

  return (
    <nav className={`${styles.navbar} ${scrolled ? styles.scrolled : ''}`}>
      <div className={styles.container}>
        <Link to="/" className={styles.logo}>
          <img src={LogoImg} alt="BookBud logo" className={styles.logoImage} />
          <span className={styles.logoText}>BookBud</span>
        </Link>

        <ul className={styles.navLinks}>
          {NAV_ITEMS.map((item) => (
            <li key={item.label}>
              <a
                href={item.href}
                className={`${styles.navLink} ${isActiveHash(item.href) ? styles.navLinkActive : ''}`}
              >
                {item.label}
              </a>
            </li>
          ))}
        </ul>

        {isAuthenticated ? (
          <div className={styles.userSection} ref={dropdownRef}>
            <button
              className={styles.avatarButton}
              onClick={() => setDropdownOpen((prev) => !prev)}
              aria-label="User menu"
            >
              {userInitial}
            </button>
            {dropdownOpen && (
              <div className={styles.dropdown}>
                <Link
                  to="/dashboard"
                  className={styles.dropdownItem}
                  onClick={() => setDropdownOpen(false)}
                >
                  Profile
                </Link>
                <button className={styles.dropdownItem} onClick={onLogout}>
                  Logout
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className={styles.authButtons}>
            {onSignInClick ? (
              <button className={styles.signInButton} onClick={onSignInClick}>Sign in</button>
            ) : (
              <Link to="/?auth=login" className={styles.signInButton}>Sign in</Link>
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
