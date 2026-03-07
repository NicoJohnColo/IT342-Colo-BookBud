import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import bookService from '../../services/bookService';
import './LandingPage.css';

const GENRES = [
  {
    name: 'Manga',
    img: 'https://images.unsplash.com/photo-1601850494422-3cf14624b0b3?w=300&q=80',
  },
  {
    name: 'Comics',
    img: 'https://images.unsplash.com/photo-1612036782180-6f0b6cd846fe?w=300&q=80',
  },
  {
    name: 'Fiction',
    img: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300&q=80',
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
    coverImage: 'https://covers.openlibrary.org/b/id/10110415-M.jpg',
  },
  {
    bookId: '2',
    title: 'Neverwhere',
    author: 'Neil Gaiman',
    condition: 'Good',
    rentPrice: 40,
    salePrice: 280,
    coverImage: 'https://covers.openlibrary.org/b/id/8739161-M.jpg',
  },
  {
    bookId: '3',
    title: 'They Both Die at the End',
    author: 'Adam Silvera',
    condition: 'New',
    rentPrice: 45,
    salePrice: 350,
    coverImage: 'https://covers.openlibrary.org/b/id/10527843-M.jpg',
  },
];

const GALLERY = [
  'https://covers.openlibrary.org/b/id/8739161-M.jpg',
  'https://covers.openlibrary.org/b/id/260-M.jpg',
  'https://covers.openlibrary.org/b/id/10527843-M.jpg',
  'https://covers.openlibrary.org/b/id/10110415-M.jpg',
];

const LandingPage = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState(SAMPLE_BOOKS);
  const [booksLoading, setBooksLoading] = useState(true);

  useEffect(() => {
    bookService
      .getAllBooks({ size: 3 })
      .then((res) => {
        if (res.data?.content?.length) {
          setBooks(res.data.content);
        }
      })
      .catch(() => {})
      .finally(() => setBooksLoading(false));
  }, []);

  return (
    <div className="landing">
      <Navbar />

      {/* ===== HERO ===== */}
      <section id="home" className="hero">
        <div className="hero-bg" />
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
          <img src="https://images.unsplash.com/photo-1495640452828-3df6795cf69b?w=400&q=80" alt="books" />
          <img src="https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&q=80" alt="books" />
          <img src="https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80" alt="books" />
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
              src="https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=460&q=80"
              alt="Reader"
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
                  src={`https://covers.openlibrary.org/b/id/260-M.jpg`}
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
                  <button className="btn-rent" onClick={() => navigate('/login')}>Rent</button>
                  <button className="btn-buy" onClick={() => navigate('/login')}>Buy</button>
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

      <Footer />
    </div>
  );
};

export default LandingPage;
