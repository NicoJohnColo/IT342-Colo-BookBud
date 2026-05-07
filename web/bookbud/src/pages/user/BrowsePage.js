import React, { useMemo, useState, useCallback, useEffect } from 'react';
import './BrowsePage.css';
import { resolveBookImageUrl } from '../../utils/bookImage';
import wishlistService from '../../services/wishlistService';

const asNumber = (value) => Number(value || 0);
const toLower = (value) => String(value || '').toLowerCase();

const isOwnedByCurrentUser = (book, currentUserId) => String(book?.ownerId || '') === String(currentUserId || '');
const isAvailable = (book) => toLower(book?.status) === 'available';
const PAYMENT_METHODS = [
  { value: 'cash', label: 'Cash', apiValue: 'Cash' },
  { value: 'gcash', label: 'GCash', apiValue: 'GCash' },
  { value: 'bank_transfer', label: 'Bank Transfer', apiValue: 'Bank Transfer' },
];
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
  const [selectedType, setSelectedType] = useState('all');
  const [selectedGenre, setSelectedGenre] = useState('all');
  const [selectedCondition, setSelectedCondition] = useState('all');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [selectedBook, setSelectedBook] = useState(null);
  const [selectedMode, setSelectedMode] = useState('rent');
  const [paymentMethod, setPaymentMethod] = useState('cash');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [wishlistLoading, setWishlistLoading] = useState({});

  useEffect(() => {
    const saved = localStorage.getItem('bookbud-browse-filters');
    if (!saved) return;
    try {
      const parsed = JSON.parse(saved);
      setQuery(parsed.query || '');
      setSelectedType(parsed.selectedType || 'all');
      setSelectedGenre(parsed.selectedGenre || 'all');
      setSelectedCondition(parsed.selectedCondition || 'all');
      setMinPrice(parsed.minPrice || '');
      setMaxPrice(parsed.maxPrice || '');
    } catch {
      // Ignore malformed saved state.
    }
  }, []);

  useEffect(() => {
    const payload = {
      query,
      selectedType,
      selectedGenre,
      selectedCondition,
      minPrice,
      maxPrice,
    };
    localStorage.setItem('bookbud-browse-filters', JSON.stringify(payload));
  }, [maxPrice, minPrice, query, selectedCondition, selectedGenre, selectedType]);

  const clearFilters = () => {
    setQuery('');
    setSelectedType('all');
    setSelectedGenre('all');
    setSelectedCondition('all');
    setMinPrice('');
    setMaxPrice('');
  };

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
    const q = query.toLowerCase();
    const min = minPrice ? Number(minPrice) : null;
    const max = maxPrice ? Number(maxPrice) : null;

    return books.filter((book) => {
      const matchesQuery = !q || [book.title, book.author, book.genre].some((v) => String(v || '').toLowerCase().includes(q));
      if (!matchesQuery) return false;

      const type = toLower(book?.transactionType);
      const supportsType =
        selectedType === 'all' ||
        (selectedType === 'rent' && (type === 'rent' || type === 'both')) ||
        (selectedType === 'sale' && (type === 'sale' || type === 'both'));
      if (!supportsType) return false;

      const matchesGenre = selectedGenre === 'all' || toLower(book?.genre) === selectedGenre;
      if (!matchesGenre) return false;

      const matchesCondition = selectedCondition === 'all' || toLower(book?.condition) === selectedCondition;
      if (!matchesCondition) return false;

      const rentPrice = Number(book?.priceRent || 0);
      const salePrice = Number(book?.priceSale || 0);
      const availablePrices = [rentPrice, salePrice].filter((value) => value > 0);
      const priceValue = selectedType === 'rent'
        ? rentPrice
        : selectedType === 'sale'
          ? salePrice
          : (availablePrices.length ? Math.min(...availablePrices) : 0);

      if (min !== null && priceValue < min) return false;
      if (max !== null && priceValue > max) return false;

      return true;
    });
  }, [books, maxPrice, minPrice, query, selectedCondition, selectedGenre, selectedType]);

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
          <h2 className="page-title">Browse</h2>
          <p className="page-subtitle">Discover books from the community</p>
        </div>
        <div className="search-inline">
          <span>🔍</span>
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search books" />
        </div>
      </div>

      <div className="browse-filters">
        <div className="filter-group">
          <span className="filter-label">Type</span>
          {['all', 'rent', 'sale'].map((type) => (
            <button
              key={type}
              type="button"
              className={`filter-chip ${selectedType === type ? 'active' : ''}`}
              onClick={() => setSelectedType(type)}
            >
              {type === 'all' ? 'All' : type === 'rent' ? 'For Rent' : 'For Sale'}
            </button>
          ))}
        </div>

        <div className="filter-group">
          <span className="filter-label">Genre</span>
          {['all', 'fiction', 'fantasy', 'drama', 'mystery', 'thriller', 'biography', 'self-help', 'classic'].map((genre) => (
            <button
              key={genre}
              type="button"
              className={`filter-chip ${selectedGenre === genre ? 'active' : ''}`}
              onClick={() => setSelectedGenre(genre)}
            >
              {genre === 'all' ? 'All' : genre.replace('-', ' ')}
            </button>
          ))}
        </div>

        <div className="filter-group">
          <span className="filter-label">Condition</span>
          {['all', 'new', 'like new', 'good', 'fair', 'poor'].map((condition) => (
            <button
              key={condition}
              type="button"
              className={`filter-chip ${selectedCondition === condition ? 'active' : ''}`}
              onClick={() => setSelectedCondition(condition)}
            >
              {condition === 'all' ? 'All' : condition}
            </button>
          ))}
        </div>

        <div className="filter-group filter-prices">
          <span className="filter-label">Price Range</span>
          <div className="filter-price-inputs">
            <input
              type="number"
              min="0"
              placeholder="Min"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
            />
            <span className="filter-range-sep">-</span>
            <input
              type="number"
              min="0"
              placeholder="Max"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </div>
        </div>

        <button type="button" className="filter-clear" onClick={clearFilters}>
          Clear Filters
        </button>
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
                {PAYMENT_METHODS.map((method) => (
                  <button
                    key={method.value}
                    type="button"
                    className={`type-btn ${paymentMethod === method.value ? 'active' : ''}`}
                    onClick={() => setPaymentMethod(method.value)}
                  >
                    {method.label}
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
