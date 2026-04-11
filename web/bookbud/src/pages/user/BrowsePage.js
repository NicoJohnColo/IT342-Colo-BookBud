import React, { useMemo, useState, useCallback } from 'react';
import './BrowsePage.css';
import { resolveBookImageUrl } from '../../utils/bookImage';
import wishlistService from '../../services/wishlistService';

const asNumber = (value) => Number(value || 0);
const toLower = (value) => String(value || '').toLowerCase();

const isOwnedByCurrentUser = (book, currentUserId) => String(book?.ownerId || '') === String(currentUserId || '');
const isAvailable = (book) => toLower(book?.status) === 'available';
const availabilityLabel = (book) => {
  const status = toLower(book?.status);
  if (status === 'sold') return 'Purchased';
  if (status && status !== 'available') return 'Unavailable';
  return null;
};
const supportsRent = (book) => {
  const type = toLower(book?.transactionType);
  return type === 'rent' || type === 'both';
};
const supportsBuy = (book) => {
  const type = toLower(book?.transactionType);
  return type === 'sale' || type === 'both';
};

export default function BrowsePage({ books = [], currentUserId, onCreateTransaction, wishlist = [], onWishlistChange }) {
  const [query, setQuery] = useState('');
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
    const item = wishlist.find((item) => item.bookId === bookId || item.book?.bookId === bookId);
    return item?.wishlistId;
  }, [wishlist]);

  const toggleWishlist = async (book) => {
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
  };

  const filtered = useMemo(() => {
    if (!query.trim()) return books;
    const q = query.toLowerCase();
    return books.filter((b) => [b.title, b.author, b.genre].some((v) => String(v || '').toLowerCase().includes(q)));
  }, [books, query]);

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
        setModalError('End date must be on or after the start date.');
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
        message: `${selectedMode === 'rent' ? 'Rental' : 'Purchase'} request submitted. Please wait for the owner to confirm.`,
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
          <h2 className="page-title">Browse</h2>
          <p className="page-subtitle">Discover books from the community</p>
        </div>
        <div className="search-inline">
          <span>🔍</span>
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search books" />
        </div>
      </div>

      {feedback ? <div className={`browse-feedback ${feedback.type}`}>{feedback.message}</div> : null}

      {!filtered.length && <div className="empty-state">No books found.</div>}

      <div className="book-grid">
        {filtered.map((book) => {
          const ownListing = isOwnedByCurrentUser(book, currentUserId);
          const available = isAvailable(book);
          const currentAvailability = availabilityLabel(book);
          const showWishlistButton = available && !ownListing;
          const canRent = supportsRent(book) && !ownListing && available;
          const canBuy = supportsBuy(book) && !ownListing && available;

          return (
            <div className="browse-card" key={book.bookId}>
              <div className="browse-cover">
                {book.imageUrl ? <img src={resolveBookImageUrl(book.imageUrl)} alt={book.title} /> : <div className="browse-cover-placeholder" />}
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
              <div className="browse-info">
                <div className="browse-badges">
                  <span className={`owner-indicator ${ownListing ? 'mine' : 'other'}`}>
                    {ownListing ? 'Your Listing' : 'Other Seller'}
                  </span>
                  {currentAvailability ? (
                    <span className={`availability-indicator ${currentAvailability === 'Purchased' ? 'purchased' : 'unavailable'}`}>
                      {currentAvailability}
                    </span>
                  ) : null}
                  {supportsRent(book) ? <span className="txn-indicator">Rent</span> : null}
                  {supportsBuy(book) ? <span className="txn-indicator sale">Buy</span> : null}
                </div>
                <div className="browse-title">{book.title}</div>
                <div className="browse-author">{book.author}</div>
                <div className="browse-price">PHP {asNumber(book.priceSale || book.priceRent)}</div>
                <div className="browse-location">{book.genre || 'General'}</div>

                <div className="browse-actions">
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

                {ownListing ? <div className="browse-note">You cannot buy or rent your own listing.</div> : null}
                {!available ? (
                  <div className="browse-note">
                    {currentAvailability === 'Purchased'
                      ? 'This book has already been purchased.'
                      : 'This book is currently unavailable.'}
                  </div>
                ) : null}
              </div>
            </div>
          );
        })}
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
                <button
                  className={`type-btn ${selectedMode === 'rent' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setSelectedMode('rent')}
                >
                  Rent
                </button>
              ) : null}
              {selectedBookSupportsBuy ? (
                <button
                  className={`type-btn ${selectedMode === 'buy' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setSelectedMode('buy')}
                >
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
              <button className="btn btn-ghost" type="button" onClick={closeModal}>
                Cancel
              </button>
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
