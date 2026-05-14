import React, { useState } from 'react';
import './Footer.css';

const Footer = () => {
  const [email, setEmail] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (email) {
      setSubmitted(true);
      setEmail('');
    }
  };

  return (
    <footer className="footer">
      {/* Mailing List */}
      <div className="footer-mailing">
        <h2>Join Our Mailing List</h2>
        <p>Sign up to receive new book alerts, reading inspiration, and special offers from our team</p>
        {submitted ? (
          <p className="footer-success">Thanks for subscribing!</p>
        ) : (
          <form className="footer-form" onSubmit={handleSubmit}>
            <input
              type="email"
              placeholder="example@gmail.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <button type="submit">Submit</button>
          </form>
        )}
      </div>

      {/* Orange bottom */}
      <div className="footer-bottom">
        <div className="footer-bottom-inner">
          <div className="footer-follow">
            <h4>Follow Us</h4>
            <div className="footer-social-links">
              <a href="#!" aria-label="Facebook">Facebook</a>
              <a href="#!" aria-label="Instagram">Instagram</a>
              <a href="#!" aria-label="Twitter">Twitter</a>
            </div>
            <div className="footer-legal">
              <p>Copyright © 2026 BookBud · <a href="#!">Terms &amp; Conditions</a></p>
              <a href="#!">Privacy Policy</a>
            </div>
          </div>

          <div className="footer-instagram">
            <h4>Instagram Shop</h4>
            <div className="footer-insta-grid">
              {[
                'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=120&q=80',
                'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=120&q=80',
                'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=120&q=80',
                'https://images.unsplash.com/photo-1495640452828-3df6795cf69b?w=120&q=80',
              ].map((src, i) => (
                <a key={i} href="#!" className="footer-insta-item">
                  <img src={src} alt={`Instagram ${i + 1}`} />
                </a>
              ))}
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
