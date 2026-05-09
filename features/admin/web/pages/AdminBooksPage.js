import React, { useState, useEffect } from 'react';
import adminService from '../../services/adminService';
import './AdminPages.css';

export default function AdminBooksPage({ books = [], onRefresh }) {
  const [filteredBooks, setFilteredBooks] = useState(books);
  const [selectedStatus, setSelectedStatus] = useState('All');
  const [loading, setLoading] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [selectedBook, setSelectedBook] = useState(null);
  const [statusModal, setStatusModal] = useState(null);
  const [newStatus, setNewStatus] = useState('Available');

  // Sync filtered books when books prop changes
  useEffect(() => {
    if (selectedStatus === 'All') {
      setFilteredBooks(books);
    } else {
      setFilteredBooks(books.filter(b => b.status === selectedStatus));
    }
  }, [books, selectedStatus]);

  const statuses = ['All', 'Available', 'Rented', 'Sold', 'Unavailable'];

  const handleFilterChange = (status) => {
    setSelectedStatus(status);
    if (status === 'All') {
      setFilteredBooks(books);
    } else {
      setFilteredBooks(books.filter(b => b.status === status));
    }
  };

  const handleStatusChange = async (bookId, status) => {
    setLoading(true);
    try {
      await adminService.updateBookStatus(bookId, status);
      setToastMessage(`Book status updated to ${status}`);
      setStatusModal(null);
      setNewStatus('Available');
      if (onRefresh) onRefresh();
    } catch (error) {
      setToastMessage(`Error: ${error.response?.data?.error?.message || 'Failed to update status'}`);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (bookId) => {
    if (window.confirm('Are you sure you want to delete this book listing?')) {
      setLoading(true);
      try {
        await adminService.deleteBook(bookId);
        setToastMessage('Book deleted successfully');
        if (onRefresh) onRefresh();
      } catch (error) {
        setToastMessage(`Error: ${error.response?.data?.error?.message || 'Failed to delete book'}`);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="admin-page">
      <h2 className="page-title">Book Management</h2>
      <p className="page-subtitle">{filteredBooks.length} total listings</p>

      <div className="filter-tabs">
        {statuses.map((status) => (
          <button
            key={status}
            className={`filter-tab ${selectedStatus === status ? 'active' : ''}`}
            onClick={() => handleFilterChange(status)}
          >
            {status}
            {status !== 'All' && <span className="badge">{books.filter(b => b.status === status).length}</span>}
          </button>
        ))}
      </div>

      <div className="admin-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Book ID</th>
              <th>Title</th>
              <th>Author</th>
              <th>Owner</th>
              <th>Condition</th>
              <th>Type</th>
              <th>Status</th>
              <th>Listed Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredBooks.length > 0 ? (
              filteredBooks.map((book) => (
                <tr key={book.bookId}>
                  <td className="mono">{book.bookId?.substring(0, 8)}</td>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td>{book.ownerId?.substring(0, 8) || 'N/A'}</td>
                  <td><span className="badge-condition">{book.condition}</span></td>
                  <td><span className="badge-type">{book.transactionType}</span></td>
                  <td>
                    <span className={`badge-status status-${(book.status || 'unknown').toLowerCase()}`}>
                      {book.status}
                    </span>
                  </td>
                  <td className="text-muted">{new Date(book.createdAt).toLocaleDateString()}</td>
                  <td className="actions">
                    <button className="btn-small btn-primary" onClick={() => setSelectedBook(book)}>
                      View
                    </button>
                    <button 
                      className="btn-small btn-warning" 
                      onClick={() => {
                        setStatusModal(book);
                        setNewStatus(book.status || 'Available');
                      }}
                      disabled={book.status === 'Unavailable'}
                    >
                      Set Unavailable
                    </button>
                    <button className="btn-small btn-danger" onClick={() => handleDelete(book.bookId)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="9" className="text-center text-muted">No books found</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Book Detail Modal */}
      {selectedBook && (
        <div className="modal-overlay" onClick={() => setSelectedBook(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Book Detail</h3>
              <button className="close-btn" onClick={() => setSelectedBook(null)}>×</button>
            </div>
            <div className="modal-body">
              <p><strong>Book ID:</strong> {selectedBook.bookId}</p>
              <p><strong>Title:</strong> {selectedBook.title}</p>
              <p><strong>Author:</strong> {selectedBook.author}</p>
              <p><strong>Genre:</strong> {selectedBook.genre}</p>
              <p><strong>Condition:</strong> {selectedBook.condition}</p>
              <p><strong>Transaction Type:</strong> {selectedBook.transactionType}</p>
              <p><strong>Price (Rent):</strong> ${selectedBook.priceRent || 'N/A'}</p>
              <p><strong>Price (Sale):</strong> ${selectedBook.priceSale || 'N/A'}</p>
              <p><strong>Status:</strong> {selectedBook.status}</p>
              <p><strong>Owner ID:</strong> {selectedBook.ownerId}</p>
              <p><strong>Listed:</strong> {new Date(selectedBook.createdAt).toLocaleString()}</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelectedBook(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Status Update Modal */}
      {statusModal && (
        <div className="modal-overlay" onClick={() => setStatusModal(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Update Book Status</h3>
              <button className="close-btn" onClick={() => setStatusModal(null)}>×</button>
            </div>
            <div className="modal-body">
              <p>Change status for: <strong>{statusModal.title}</strong></p>
              <p>Current Status: <strong>{statusModal.status}</strong></p>
              <p>New Status:</p>
              <select 
                className="form-control" 
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
              >
                <option value="Available">Available</option>
                <option value="Rented">Rented</option>
                <option value="Sold">Sold</option>
                <option value="Unavailable">Unavailable</option>
              </select>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setStatusModal(null)}>Cancel</button>
              <button 
                className="btn btn-primary" 
                onClick={() => handleStatusChange(statusModal.bookId, newStatus)}
                disabled={loading}
              >
                {loading ? 'Updating...' : 'Update Status'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Toast Notification */}
      {toastMessage && (
        <div className="toast-notification">
          {toastMessage}
          <button className="close-btn" onClick={() => setToastMessage('')}>×</button>
        </div>
      )}
    </div>
  );
}
