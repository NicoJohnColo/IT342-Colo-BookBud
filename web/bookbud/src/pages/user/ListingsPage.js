import React, { useMemo, useState } from 'react';
import './ListingsPage.css';
import { resolveBookImageUrl } from '../../utils/bookImage';

const asNumber = (value) => Number(value || 0);

const INITIAL_FORM = {
  title: '',
  author: '',
  description: '',
  genre: 'Fiction',
  condition: 'Good',
  transactionType: 'both',
  priceRent: '',
  priceSale: '',
};

export default function ListingsPage({ listings = [], onCreateListing, onUpdateListing, saving = false }) {
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [form, setForm] = useState(INITIAL_FORM);
  const [formError, setFormError] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState('');

  const isEditing = useMemo(() => !!editingBook, [editingBook]);

  const openAddModal = () => {
    setForm(INITIAL_FORM);
    setFormError('');
    setEditingBook(null);
    setSelectedImage(null);
    setImagePreview('');
    setIsAddOpen(true);
  };

  const openEditModal = (book) => {
    setForm({
      title: book.title || '',
      author: book.author || '',
      description: book.description || '',
      genre: book.genre || 'Fiction',
      condition: book.condition || 'Good',
      transactionType: String(book.transactionType || 'both').toLowerCase(),
      priceRent: book.priceRent ?? '',
      priceSale: book.priceSale ?? '',
    });
    setFormError('');
    setEditingBook(book);
    setSelectedImage(null);
    setImagePreview(resolveBookImageUrl(book.imageUrl));
    setIsAddOpen(true);
  };

  const closeModal = () => {
    if (saving) return;
    setIsAddOpen(false);
    setEditingBook(null);
    setFormError('');
    setSelectedImage(null);
    setImagePreview('');
  };

  const setField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const onImageChange = (event) => {
    const file = event.target.files?.[0] || null;
    setSelectedImage(file);

    if (!file) {
      setImagePreview(editingBook ? resolveBookImageUrl(editingBook.imageUrl) : '');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => setImagePreview(String(reader.result || ''));
    reader.readAsDataURL(file);
  };

  const validate = () => {
    if (!form.title.trim()) return 'Title is required.';
    if (!form.author.trim()) return 'Author is required.';
    if (!form.condition.trim()) return 'Condition is required.';
    if (!form.transactionType) return 'Transaction type is required.';
    if (form.description && form.description.length > 1000) return 'Description must be at most 1000 characters.';

    const needsRent = form.transactionType === 'rent' || form.transactionType === 'both';
    const needsSale = form.transactionType === 'sale' || form.transactionType === 'both';

    if (needsRent && (!form.priceRent || Number(form.priceRent) <= 0)) {
      return 'Rental price must be greater than 0.';
    }

    if (needsSale && (!form.priceSale || Number(form.priceSale) <= 0)) {
      return 'Sale price must be greater than 0.';
    }

    return '';
  };

  const handleSubmit = async () => {
    const error = validate();
    if (error) {
      setFormError(error);
      return;
    }

    const payload = {
      title: form.title.trim(),
      author: form.author.trim(),
      description: form.description.trim() || null,
      genre: form.genre,
      condition: form.condition,
      transactionType: form.transactionType,
      priceRent: form.transactionType === 'sale' ? null : Number(form.priceRent),
      priceSale: form.transactionType === 'rent' ? null : Number(form.priceSale),
    };

    try {
      if (isEditing) {
        await onUpdateListing?.(editingBook.bookId, payload, selectedImage);
      } else {
        await onCreateListing?.(payload, selectedImage);
      }
      closeModal();
    } catch {
      setFormError('Could not save listing. Please try again.');
    }
  };

  return (
    <div>
      <div className="top-bar">
        <div>
          <h2 className="page-title">My Listings</h2>
          <p className="page-subtitle">{listings.length} books listed</p>
        </div>
        <button className="btn btn-primary" onClick={openAddModal}>Add New Book</button>
      </div>

      {!listings.length && <div className="empty-state">You do not have listings yet.</div>}

      <div className="listing-grid">
        {listings.map((book) => (
          <div key={book.bookId} className="listing-card">
            <div className="listing-cover">
              {book.imageUrl ? <img src={resolveBookImageUrl(book.imageUrl)} alt={book.title} /> : <div className="listing-cover-placeholder" />}
            </div>
            <div className="listing-info">
              <div className="listing-title">{book.title}</div>
              <div className="listing-author">{book.author}</div>
              {book.description ? <div className="listing-description">{book.description}</div> : null}
              <div className="listing-price">PHP {asNumber(book.priceSale || book.priceRent)}</div>
              <span className={`status-badge ${String(book.status || '').toLowerCase()}`}>{book.status || 'available'}</span>
              <div className="listing-actions">
                <button className="btn btn-outline btn-sm" onClick={() => openEditModal(book)}>Edit</button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {isAddOpen && (
        <div className="listing-modal-overlay" onClick={closeModal}>
          <div className="listing-modal" onClick={(e) => e.stopPropagation()}>
            <h3>{isEditing ? 'Edit Listing' : 'List a Book'}</h3>

            <div className="modal-field">
              <label>Title</label>
              <input value={form.title} onChange={(e) => setField('title', e.target.value)} placeholder="Book title" />
            </div>

            <div className="modal-field">
              <label>Author</label>
              <input value={form.author} onChange={(e) => setField('author', e.target.value)} placeholder="Author name" />
            </div>

            <div className="modal-field">
              <label>Description (optional)</label>
              <textarea
                value={form.description}
                onChange={(e) => setField('description', e.target.value)}
                placeholder="Short description of your book"
                maxLength={1000}
                rows={3}
              />
            </div>

            <div className="modal-field">
              <label>Cover Image (optional)</label>
              <input type="file" accept="image/*" onChange={onImageChange} />
              {imagePreview ? (
                <div className="listing-image-preview-wrap">
                  <img src={imagePreview} alt="Book cover preview" className="listing-image-preview" />
                </div>
              ) : null}
            </div>

            <div className="modal-row">
              <div className="modal-field">
                <label>Genre</label>
                <select value={form.genre} onChange={(e) => setField('genre', e.target.value)}>
                  {['Fiction', 'Fantasy', 'Drama', 'Mystery', 'Thriller', 'Biography', 'Self-Help', 'Classic'].map((genre) => (
                    <option key={genre} value={genre}>{genre}</option>
                  ))}
                </select>
              </div>
              <div className="modal-field">
                <label>Condition</label>
                <select value={form.condition} onChange={(e) => setField('condition', e.target.value)}>
                  {['New', 'Like New', 'Good', 'Fair', 'Poor'].map((condition) => (
                    <option key={condition} value={condition}>{condition}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="modal-field">
              <label>Transaction Type</label>
              <div className="type-toggle">
                {['rent', 'sale', 'both'].map((type) => (
                  <button
                    key={type}
                    type="button"
                    className={`type-btn ${form.transactionType === type ? 'active' : ''}`}
                    onClick={() => setField('transactionType', type)}
                  >
                    {type.charAt(0).toUpperCase() + type.slice(1)}
                  </button>
                ))}
              </div>
            </div>

            <div className="modal-row">
              <div className="modal-field">
                <label>Rental Price (P/day)</label>
                <input
                  type="number"
                  value={form.priceRent}
                  onChange={(e) => setField('priceRent', e.target.value)}
                  placeholder="e.g. 40"
                  disabled={form.transactionType === 'sale'}
                />
              </div>
              <div className="modal-field">
                <label>Sale Price (P)</label>
                <input
                  type="number"
                  value={form.priceSale}
                  onChange={(e) => setField('priceSale', e.target.value)}
                  placeholder="e.g. 180"
                  disabled={form.transactionType === 'rent'}
                />
              </div>
            </div>

            {formError && <p className="modal-error">{formError}</p>}

            <div className="modal-actions">
              <button type="button" className="btn btn-ghost" onClick={closeModal} disabled={saving}>Cancel</button>
              <button type="button" className="btn btn-primary" onClick={handleSubmit} disabled={saving}>
                {saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Publish Listing'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
