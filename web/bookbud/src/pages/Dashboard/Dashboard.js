import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import bookService from '../../services/bookService';
import './Dashboard.css';

const TABS = ['Overview', 'Browse Books', 'My Listings', 'Wishlist'];

const Dashboard = () => {
  const { user, handleLogout: doLogout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('Overview');
  const [books, setBooks] = useState([]);
  const [booksLoading, setBooksLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  const fetchBooks = useCallback(async () => {
    setBooksLoading(true);
    try {
      const res = await bookService.getAllBooks({ size: 20 });
      setBooks(res.data?.content || []);
    } catch {
      setBooks([]);
    } finally {
      setBooksLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!user) { navigate('/?auth=login'); return; }
    fetchBooks();
  }, [user, navigate, fetchBooks]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setSearching(true);
    try {
      const res = await bookService.getAllBooks({ q: searchQuery, size: 20 });
      setSearchResults(res.data?.content || []);
      setActiveTab('Browse Books');
    } catch {
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  };

  const handleLogout = async () => {
    await doLogout();
    navigate('/');
  };

  const displayedBooks = searchQuery && searchResults.length >= 0 && activeTab === 'Browse Books'
    ? searchResults
    : books;

  const myListings = books.filter((b) => b.ownerId === user?.userId || b.ownerUsername === user?.username);

  const initials = user?.username
    ? user.username.slice(0, 2).toUpperCase()
    : 'BB';

  return (
    <div className="dashboard">
      {/* ===== SIDEBAR ===== */}
      <aside className={`dash-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="dash-sidebar-header">
          <span className="dash-logo"><span>B</span><span>B</span></span>
          <span className="dash-logo-text">BookBud</span>
        </div>

        <nav className="dash-nav">
          {TABS.map((tab) => (
            <button
              key={tab}
              className={`dash-nav-item ${activeTab === tab ? 'active' : ''}`}
              onClick={() => { setActiveTab(tab); setSidebarOpen(false); }}
            >
              <span className="dash-nav-icon">{tabIcon(tab)}</span>
              {tab}
            </button>
          ))}
        </nav>

        <div className="dash-sidebar-footer">
          <button className="dash-nav-item dash-logout" onClick={handleLogout}>
            <span className="dash-nav-icon">🚪</span>
            Sign Out
          </button>
        </div>
      </aside>

      {/* ===== MAIN ===== */}
      <div className="dash-main">
        {/* Topbar */}
        <header className="dash-topbar">
          <button className="dash-hamburger" onClick={() => setSidebarOpen(!sidebarOpen)} aria-label="Menu">
            <span /><span /><span />
          </button>

          <form className="dash-search" onSubmit={handleSearch}>
            <input
              type="text"
              placeholder="Search books..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" disabled={searching}>
              {searching ? '...' : '🔍'}
            </button>
          </form>

          <div className="dash-topbar-right">
            <div className="dash-avatar" title={user?.username}>{initials}</div>
            <div className="dash-user-info">
              <strong>{user?.username}</strong>
              <span>{user?.email}</span>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="dash-content">
          {activeTab === 'Overview' && (
            <OverviewTab user={user} books={books} myListings={myListings} setActiveTab={setActiveTab} />
          )}
          {activeTab === 'Browse Books' && (
            <BooksTab books={displayedBooks} loading={booksLoading} query={searchQuery} />
          )}
          {activeTab === 'My Listings' && (
            <MyListingsTab listings={myListings} navigate={navigate} />
          )}
          {activeTab === 'Wishlist' && (
            <WishlistTab />
          )}
        </main>
      </div>

      {/* Overlay for mobile sidebar */}
      {sidebarOpen && <div className="dash-overlay" onClick={() => setSidebarOpen(false)} />}
    </div>
  );
};

/* ===== TAB ICONS ===== */
const tabIcon = (tab) => {
  const icons = { Overview: '🏠', 'Browse Books': '📚', 'My Listings': '📋', Wishlist: '❤️' };
  return icons[tab] || '📄';
};

/* ===== OVERVIEW TAB ===== */
const OverviewTab = ({ user, books, myListings, setActiveTab }) => (
  <div className="overview-tab">
    <h1 className="dash-page-title">Welcome back, {user?.username}! 👋</h1>
    <p className="dash-page-sub">Here's what's happening on BookBud today.</p>

    <div className="overview-stats">
      <StatCard icon="📚" label="Total Books" value={books.length} color="#f97316" />
      <StatCard icon="📋" label="My Listings" value={myListings.length} color="#6366f1" />
      <StatCard icon="❤️" label="Wishlist" value={0} color="#ec4899" />
      <StatCard icon="💫" label="Transactions" value={0} color="#10b981" />
    </div>

    <div className="overview-sections">
      <div className="overview-card">
        <div className="overview-card-header">
          <h3>Recent Books</h3>
          <button className="link-btn" onClick={() => setActiveTab('Browse Books')}>View all</button>
        </div>
        <div className="recent-books-list">
          {books.slice(0, 4).map((book) => (
            <RecentBookRow key={book.bookId} book={book} />
          ))}
          {books.length === 0 && <p className="empty-msg">No books available.</p>}
        </div>
      </div>

      <div className="overview-card profile-card">
        <h3>My Profile</h3>
        <div className="profile-avatar-l">{user?.username?.slice(0, 2).toUpperCase() || 'BB'}</div>
        <div className="profile-fields">
          <p><strong>Username:</strong> {user?.username}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          <p><strong>Role:</strong> {user?.role}</p>
          <p><strong>Member since:</strong> {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}</p>
        </div>
      </div>
    </div>
  </div>
);

const StatCard = ({ icon, label, value, color }) => (
  <div className="stat-card" style={{ '--accent': color }}>
    <span className="stat-icon">{icon}</span>
    <div>
      <span className="stat-value">{value}</span>
      <p className="stat-label">{label}</p>
    </div>
  </div>
);

const RecentBookRow = ({ book }) => (
  <div className="recent-book-row">
    <img
      src={'https://covers.openlibrary.org/b/id/260-M.jpg'}
      alt={book.title}
      onError={(e) => { e.target.src = 'https://covers.openlibrary.org/b/id/260-M.jpg'; }}
    />
    <div className="recent-book-info">
      <strong>{book.title}</strong>
      <span>{book.author}</span>
    </div>
    <span className="recent-book-price">
      {book.priceRent ? `₱${book.priceRent}/wk` : '—'}
    </span>
  </div>
);

/* ===== BROWSE BOOKS TAB ===== */
const BooksTab = ({ books, loading, query }) => (
  <div className="books-tab">
    <h1 className="dash-page-title">Browse Books</h1>
    <p className="dash-page-sub">{query ? `Results for "${query}"` : 'Discover books listed by our community.'}</p>
    {loading ? (
      <div className="loading-spinner">Loading books...</div>
    ) : (
      <div className="dash-books-grid">
        {books.map((book) => (
          <DashBookCard key={book.bookId} book={book} />
        ))}
        {!books.length && <p className="empty-msg">No books found.</p>}
      </div>
    )}
  </div>
);

const DashBookCard = ({ book }) => (
  <div className="dash-book-card">
    <div className="dash-book-cover">
      <img
        src={'https://covers.openlibrary.org/b/id/260-M.jpg'}
        alt={book.title}
        onError={(e) => { e.target.src = 'https://covers.openlibrary.org/b/id/260-M.jpg'; }}
      />
      <span className="dash-book-status">{book.status || 'Available'}</span>
    </div>
    <div className="dash-book-body">
      <h4>{book.title}</h4>
      <p className="dash-book-author">{book.author}</p>
      <div className="dash-book-meta">
        {book.priceRent && (
          <span className="price-tag rent">Rent ₱{book.priceRent}</span>
        )}
        {book.priceSale && (
          <span className="price-tag buy">Buy ₱{book.priceSale}</span>
        )}
      </div>
      <div className="dash-book-actions">
        <button className="dash-btn-rent">Rent</button>
        <button className="dash-btn-buy">Buy</button>
      </div>
    </div>
  </div>
);

/* ===== MY LISTINGS TAB ===== */
const MyListingsTab = ({ listings }) => (
  <div className="listings-tab">
    <div className="tab-header-row">
      <div>
        <h1 className="dash-page-title">My Listings</h1>
        <p className="dash-page-sub">Books you have listed for rent or sale.</p>
      </div>
      <button className="btn-add-listing" onClick={() => alert('Coming soon!')}>+ Add Listing</button>
    </div>
    {listings.length === 0 ? (
      <div className="empty-state">
        <span>📚</span>
        <p>You haven't listed any books yet.</p>
        <button className="btn-add-listing" onClick={() => alert('Coming soon!')}>List a Book</button>
      </div>
    ) : (
      <div className="dash-books-grid">
        {listings.map((book) => (
          <DashBookCard key={book.bookId} book={book} />
        ))}
      </div>
    )}
  </div>
);

/* ===== WISHLIST TAB ===== */
const WishlistTab = () => (
  <div className="wishlist-tab">
    <h1 className="dash-page-title">My Wishlist</h1>
    <p className="dash-page-sub">Books you've saved for later.</p>
    <div className="empty-state">
      <span>❤️</span>
      <p>Your wishlist is empty. Browse books and save your favorites!</p>
    </div>
  </div>
);

export default Dashboard;
