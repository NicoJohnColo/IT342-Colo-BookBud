import React from 'react';
import './WishlistPage.css';
import { resolveBookImageUrl } from '../../utils/bookImage';

const asNumber = (value) => Number(value || 0);

export default function WishlistPage({ wishlist = [], onRemove }) {
  return (
    <div>
      <h2 className="page-title">Wishlist</h2>
      <p className="page-subtitle">{wishlist.length} saved books</p>

      {!wishlist.length && <div className="empty-state">No saved books yet.</div>}

      <div className="wishlist-grid">
        {wishlist.map((item) => (
          <div key={item.wishlistId} className="wishlist-card">
            <div className="wishlist-cover">
              {item.book?.imageUrl ? <img src={resolveBookImageUrl(item.book.imageUrl)} alt={item.book?.title || 'Book'} /> : <div className="wishlist-cover-placeholder" />}
              <div className="wishlist-heart-icon" title="Saved in wishlist" aria-label="Saved in wishlist">
                <span className="wishlist-heart-symbol">♥</span>
              </div>
            </div>
            <div className="wishlist-info">
              <div className="wishlist-title">{item.book?.title || item.bookId}</div>
              <div className="wishlist-author">{item.book?.author || 'Unknown author'}</div>
              <div className="wishlist-price">PHP {asNumber(item.book?.priceSale || item.book?.priceRent)}</div>
              <button className="btn btn-danger btn-sm" onClick={() => onRemove(item.wishlistId)}>Remove</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
