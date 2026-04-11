import React, { useCallback, useMemo, useState } from 'react';
import bothDieCover from '../../components/imgs/bothdie.png';
import harryCover from '../../components/imgs/harry.png';
import neverCover from '../../components/imgs/never.png';
import mangaCover from '../../components/imgs/manga.png';
import './OverviewPage.css';
import './BrowsePage.css';
import { resolveBookImageUrl } from '../../utils/bookImage';
import wishlistService from '../../services/wishlistService';
import paymentService from '../../services/paymentService';

const HERO_COVERS = [bothDieCover, harryCover, neverCover, mangaCover];

const asNumber = (value) => Number(value || 0);
const isOwnedByCurrentUser = (book, currentUserId) => String(book?.ownerId || '') === String(currentUserId || '');
const toLower = (value) => String(value || '').toLowerCase();

const supportsRent = (book) => {
  const type = toLower(book?.transactionType);
  return type === 'rent' || type === 'both';
};

const supportsBuy = (book) => {
  const type = toLower(book?.transactionType);
  return type === 'sale' || type === 'both';
};

const isAvailable = (book) => toLower(book?.status) === 'available';
const availabilityLabel = (book) => {
  const status = toLower(book?.status);
  if (status === 'sold') return 'Purchased';
  if (status && status !== 'available') return 'Unavailable';
  return null;
};

const bookDescription = (book) => {
  const customDescription = String(book?.description || '').trim();
  if (customDescription) return customDescription;

  const offer = supportsRent(book) && supportsBuy(book)
    ? 'Rent or buy'
    : supportsRent(book)
      ? 'For rent'
      : supportsBuy(book)
        ? 'For sale'
        : 'Community listing';
  return `${offer} • ${book?.condition || 'Condition N/A'} • ${book?.genre || 'General'}`;
};

const listedAtText = (createdAt) => {
  if (!createdAt) return 'Listed recently';
  const listed = new Date(createdAt);
  if (Number.isNaN(listed.getTime())) return 'Listed recently';
  const now = new Date();
  const diffMs = now.getTime() - listed.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  if (diffDays <= 0) return 'Listed today';
  if (diffDays === 1) return 'Listed 1 day ago';
  if (diffDays < 7) return `Listed ${diffDays} days ago`;
  return `Listed on ${listed.toLocaleDateString()}`;
};

export default function OverviewPage({
  user,
  books,
  myListings,
  transactions,
  onNavigate,
  currentUserId,
  onCreateTransaction,
  wishlist = [],
  onWishlistChange,
}) {
  const activeRentals = transactions.filter((t) => String(t.status || '').toLowerCase() === 'active').length;
  const soldBooks = myListings.filter((b) => String(b.status || '').toLowerCase() === 'sold').length;
  const totalEarned = transactions
    .filter((t) => String(t.status || '').toLowerCase() === 'completed')
    .reduce((sum, t) => sum + asNumber(t.amount), 0);

  // Payment tracking
  const pendingPayments = useMemo(() => {
    return transactions.filter(
      (t) => String(t.paymentStatus || t.status || '').toLowerCase() === 'pending'
    ).length;
  }, [transactions]);

  const successfulPayments = useMemo(() => {
    return transactions.filter(
      (t) => String(t.paymentStatus || '').toLowerCase() === 'successful'
    ).length;
  }, [transactions]);

  const featured = books[0];
  const recentListings = books.slice(0, 4);
  const recentActivities = transactions.slice(0, 4);
  const featuredIsOwn = isOwnedByCurrentUser(featured, currentUserId);
  const [selectedBook, setSelectedBook] = useState(null);
  const [selectedMode, setSelectedMode] = useState('rent');
  const [paymentMethod, setPaymentMethod] = useState('cash');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [wishlistLoading, setWishlistLoading] = useState({});

  const isBookInWishlist = useCallback((bookId) => {
    return wishlist.some((item) => item.bookId === bookId || item.book?.bookId === bookId);
  }, [wishlist]);

  const getWishlistId = useCallback((bookId) => {
    const item = wishlist.find((entry) => entry.bookId === bookId || entry.book?.bookId === bookId);
    return item?.wishlistId;
  }, [wishlist]);

  const toggleWishlist = useCallback(async (book) => {
    if (!isAvailable(book) || isOwnedByCurrentUser(book, currentUserId)) return;

    const bookId = book.bookId;
    const inWishlist = isBookInWishlist(bookId);

    setWishlistLoading((prev) => ({ ...prev, [bookId]: true }));
    try {
      if (inWishlist) {
        const wishlistId = getWishlistId(bookId);
        if (wishlistId) {
          await wishlistService.removeFromWishlist(wishlistId);
        }
      } else {
        await wishlistService.addToWishlist(bookId);
      }
      onWishlistChange?.();
    } catch {
      setFeedback({
        type: 'error',
        message: 'Could not update wishlist right now. Please try again.',
      });
    } finally {
      setWishlistLoading((prev) => ({ ...prev, [bookId]: false }));
    }
  }, [currentUserId, getWishlistId, isBookInWishlist, onWishlistChange]);

  const todayIso = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const closeModal = () => {
    setSelectedBook(null);
    setModalError('');
    setSubmitting(false);
  };

  const openModal = (book, mode) => {
    setSelectedBook(book);
    setSelectedMode(mode);
    setPaymentMethod('cash');
    setStartDate(todayIso);
    setEndDate(todayIso);
    setModalError('');
  };

  const onSubmitTransaction = async () => {
    if (!selectedBook) return;

    if (isOwnedByCurrentUser(selectedBook, currentUserId)) {
      setModalError('You cannot buy or rent your own listing.');
      return;
    }

    if (!isAvailable(selectedBook)) {
      setModalError('This book is currently unavailable for transactions.');
      return;
    }

    if (selectedMode === 'rent') {
      if (!supportsRent(selectedBook)) {
        setModalError('This listing does not allow renting.');
        return;
      }
      if (!startDate || !endDate) {
        setModalError('Please select both start and end dates for rent.');
        return;
      }
      if (endDate < startDate) {
        setModalError('End date must be on or after start date.');
        return;
      }
    }

    if (selectedMode === 'buy' && !supportsBuy(selectedBook)) {
      setModalError('This listing is not available for purchase.');
      return;
    }

    const payload = {
      bookId: selectedBook.bookId,
      startDate: selectedMode === 'rent' ? startDate : todayIso,
      endDate: selectedMode === 'rent' ? endDate : null,
    };

    setSubmitting(true);
    setModalError('');
    try {
      await onCreateTransaction?.(payload);
      setFeedback({
        type: 'success',
        message: `${selectedMode === 'rent' ? 'Rental' : 'Purchase'} request submitted successfully.`,
      });
      closeModal();
    } catch (error) {
      const message =
        error?.response?.data?.error?.message ||
        error?.response?.data?.message ||
        `Could not submit ${selectedMode === 'rent' ? 'rental' : 'purchase'} request.`;
      setModalError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const selectedBookSupportsRent = supportsRent(selectedBook);
  const selectedBookSupportsBuy = supportsBuy(selectedBook);
  const selectedBookIsOwn = isOwnedByCurrentUser(selectedBook, currentUserId);
  const selectedBookIsAvailable = isAvailable(selectedBook);
  const selectedBookAvailability = availabilityLabel(selectedBook);

  return (
    <div>
      <div className="top-bar">
        <div>
          <h1 className="page-title">Good Morning, {user?.username || 'User'}</h1>
          <p className="greeting-date">{new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}</p>
        </div>
      </div>

      {feedback ? <div className={`overview-feedback ${feedback.type}`}>{feedback.message}</div> : null}

      <div className="stat-cards">
        <div className="stat-card"><span className="stat-badge active">Active</span><div className="stat-value">{myListings.length}</div><div className="stat-label">My Listings</div></div>
        <div className="stat-card"><span className="stat-badge active">Active</span><div className="stat-value">{activeRentals}</div><div className="stat-label">Active Rentals</div></div>
        <div className="stat-card"><span className="stat-badge active">Active</span><div className="stat-value">{soldBooks}</div><div className="stat-label">Books Sold</div></div>
        <div className="stat-card highlighted"><span className="stat-badge income">💰</span><div className="stat-value">PHP {totalEarned}</div><div className="stat-label">Total Earned</div></div>
        <div className="stat-card"><span className="stat-badge pending">⏳</span><div className="stat-value">{pendingPayments}</div><div className="stat-label">Pending Payments</div></div>
        <div className="stat-card"><span className="stat-badge success">✓</span><div className="stat-value">{successfulPayments}</div><div className="stat-label">Successful Payments</div></div>
      </div>

      <div className="hero-banner">
        <div>
          <div className="hero-badge">Most Wanted This Week</div>
          {featured ? (
            <div className={`hero-owner-indicator ${featuredIsOwn ? 'mine' : 'other'}`}>
              {featuredIsOwn ? 'Your Listing' : 'From Other Seller'}
            </div>
          ) : null}
          <div className="hero-title">{featured?.title || 'No featured book yet'}</div>
          <div className="hero-meta">{featured?.author || 'No author'} • {featured?.condition || 'N/A'}</div>
          <div className="hero-price">
            PHP {asNumber(featured?.priceSale)}
            <span>{featured?.priceRent ? ` or PHP ${asNumber(featured.priceRent)}/day rent` : ''}</span>
          </div>
          <button className="btn btn-primary" onClick={() => onNavigate('browse')}>Get This Book</button>
        </div>
        <div className="hero-books">
          {HERO_COVERS.map((cover, i) => (
            <div key={i} className="hero-book-thumb"><img src={cover} alt="Book cover" /></div>
          ))}
        </div>
      </div>

      <div className="overview-grid">
        <div>
          <div className="overview-section-header">
            <span className="overview-section-title">Recently Listed Near You</span>
            <button className="overview-section-link" onClick={() => onNavigate('browse')}>Browse All</button>
          </div>
          <div className="recent-grid">
            {recentListings.map((book) => {
              const ownListing = isOwnedByCurrentUser(book, currentUserId);
              const available = isAvailable(book);
              const currentAvailability = availabilityLabel(book);
              const showWishlistButton = available && !ownListing;
              const canRent = supportsRent(book) && !ownListing && available;
              const canBuy = supportsBuy(book) && !ownListing && available;

              return (
                <div key={book.bookId} className="book-card">
                  <div className="book-cover">
                    {book.imageUrl ? <img src={resolveBookImageUrl(book.imageUrl)} alt={book.title} /> : <div className="book-cover-placeholder" />}
                    {showWishlistButton ? (
                      <button
                        className={`browse-wishlist-btn ${isBookInWishlist(book.bookId) ? 'active' : ''}`}
                        onClick={() => toggleWishlist(book)}
                        disabled={wishlistLoading[book.bookId]}
                        title={isBookInWishlist(book.bookId) ? 'Remove from wishlist' : 'Add to wishlist'}
                        aria-label={isBookInWishlist(book.bookId) ? 'Remove from wishlist' : 'Add to wishlist'}
                        type="button"
                      >
                        <span className="browse-wishlist-icon">{isBookInWishlist(book.bookId) ? '♥' : '♡'}</span>
                      </button>
                    ) : null}
                  </div>
                  <div className="book-info">
                    <div className="book-badges">
                      <div className={`book-owner-indicator ${ownListing ? 'mine' : 'other'}`}>
                        {ownListing ? 'Your Listing' : 'Other Seller'}
                      </div>
                      {currentAvailability ? (
                        <span className={`availability-indicator ${currentAvailability === 'Purchased' ? 'purchased' : 'unavailable'}`}>
                          {currentAvailability}
                        </span>
                      ) : null}
                    </div>
                    <div className="book-title">{book.title}</div>
                    <div className="book-author">{book.author}</div>
                    <div className="book-desc">{bookDescription(book)}</div>
                    <div className="book-listed-at">{listedAtText(book.createdAt)}</div>
                    {!available ? (
                      <div className="book-unavailable-note">
                        {currentAvailability === 'Purchased'
                          ? 'This book has already been purchased.'
                          : 'This book is currently unavailable.'}
                      </div>
                    ) : null}
                    <div className="book-price">PHP {asNumber(book.priceSale || book.priceRent)}</div>

                    <div className="overview-book-actions">
                      {supportsRent(book) ? (
                        <button className="btn btn-sm btn-outline" disabled={!canRent} onClick={() => openModal(book, 'rent')}>
                          Rent
                        </button>
                      ) : null}
                      {supportsBuy(book) ? (
                        <button className="btn btn-sm btn-primary" disabled={!canBuy} onClick={() => openModal(book, 'buy')}>
                          Buy
                        </button>
                      ) : null}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          {!recentListings.length && <div className="empty-state">No listings yet.</div>}
        </div>

        <div>
          <div className="overview-section-header">
            <span className="overview-section-title">Recent Activities</span>
            <button className="overview-section-link" onClick={() => onNavigate('transactions')}>View All</button>
          </div>
          {recentActivities.map((txn) => (
            <div key={txn.transactionId} className="activity-item">
              <div>
                <div className="activity-title">{txn.bookTitle || txn.transactionId}</div>
                <div className="activity-meta">Listed by: {txn.ownerUsername || txn.ownerId || 'N/A'} • Role: {txn.userRole || 'user'}</div>
              </div>
              <span className={`status-badge ${String(txn.status || '').toLowerCase()}`}>{txn.status || 'pending'}</span>
            </div>
          ))}
          {!recentActivities.length && <div className="empty-state">No recent activity.</div>}
        </div>
      </div>

      {selectedBook ? (
        <div className="browse-modal-overlay" onClick={closeModal}>
          <div className="browse-modal" onClick={(e) => e.stopPropagation()}>
            <h3>{selectedMode === 'rent' ? 'Rent This Book' : 'Buy This Book'}</h3>

            <div className="browse-modal-book">
              <div className="browse-modal-cover">
                {selectedBook.imageUrl ? (
                  <img src={resolveBookImageUrl(selectedBook.imageUrl)} alt={selectedBook.title} />
                ) : (
                  <div className="browse-cover-placeholder" />
                )}
              </div>
              <div>
                <div className="browse-title">{selectedBook.title}</div>
                <div className="browse-author">{selectedBook.author}</div>
                <div className={`owner-indicator ${selectedBookIsOwn ? 'mine' : 'other'}`}>
                  {selectedBookIsOwn ? 'Your Listing' : 'Other Seller'}
                </div>
              </div>
            </div>

            <div className="browse-type-toggle">
              {selectedBookSupportsRent ? (
                <button className={`type-btn ${selectedMode === 'rent' ? 'active' : ''}`} type="button" onClick={() => setSelectedMode('rent')}>
                  Rent
                </button>
              ) : null}
              {selectedBookSupportsBuy ? (
                <button className={`type-btn ${selectedMode === 'buy' ? 'active' : ''}`} type="button" onClick={() => setSelectedMode('buy')}>
                  Buy
                </button>
              ) : null}
            </div>

            {selectedMode === 'rent' ? (
              <>
                <div className="browse-price-line">Rental Price / day: PHP {asNumber(selectedBook.priceRent)}</div>
                <div className="browse-modal-row">
                  <div className="modal-field">
                    <label>Start Date</label>
                    <input type="date" value={startDate} min={todayIso} onChange={(e) => setStartDate(e.target.value)} />
                  </div>
                  <div className="modal-field">
                    <label>End Date</label>
                    <input type="date" value={endDate} min={startDate || todayIso} onChange={(e) => setEndDate(e.target.value)} />
                  </div>
                </div>
              </>
            ) : (
              <div className="browse-price-line">Sale Price: PHP {asNumber(selectedBook.priceSale)}</div>
            )}

            <div className="modal-field">
              <label>Payment Method</label>
              <div className="browse-type-toggle">
                {['cash', 'gcash', 'bank_transfer'].map((method) => (
                  <button
                    key={method}
                    type="button"
                    className={`type-btn ${paymentMethod === method ? 'active' : ''}`}
                    onClick={() => setPaymentMethod(method)}
                  >
                    {method === 'bank_transfer' ? 'Bank Transfer' : method.toUpperCase()}
                  </button>
                ))}
              </div>
            </div>

            {!selectedBookIsAvailable ? (
              <div className="modal-error">
                {selectedBookAvailability === 'Purchased'
                  ? 'This book has already been purchased.'
                  : 'This book is currently unavailable.'}
              </div>
            ) : null}
            {selectedBookIsOwn ? <div className="modal-error">You cannot buy or rent your own listing.</div> : null}
            {modalError ? <div className="modal-error">{modalError}</div> : null}

            <div className="browse-modal-actions">
              <button className="btn btn-ghost" type="button" onClick={closeModal}>Cancel</button>
              <button
                className="btn btn-primary"
                type="button"
                disabled={
                  submitting ||
                  selectedBookIsOwn ||
                  !selectedBookIsAvailable ||
                  (selectedMode === 'rent' ? !selectedBookSupportsRent : !selectedBookSupportsBuy)
                }
                onClick={onSubmitTransaction}
              >
                {submitting
                  ? 'Submitting...'
                  : selectedMode === 'rent'
                    ? `Confirm Rental - PHP ${asNumber(selectedBook.priceRent)}/day`
                    : `Confirm Purchase - PHP ${asNumber(selectedBook.priceSale)}`}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
