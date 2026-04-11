import React, { useState } from 'react';
import adminService from '../../services/adminService';
import './AdminPages.css';

export default function AdminTransactionsPage({ transactions = [], onRefresh }) {
  const [filteredTransactions, setFilteredTransactions] = useState(transactions);
  const [selectedStatus, setSelectedStatus] = useState('All');
  const [loading, setLoading] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [selectedTransaction, setSelectedTransaction] = useState(null);

  const statuses = ['All', 'Pending', 'Active', 'Completed', 'Cancelled'];

  const handleFilterChange = (status) => {
    setSelectedStatus(status);
    if (status === 'All') {
      setFilteredTransactions(transactions);
    } else {
      setFilteredTransactions(transactions.filter(t => t.status === status));
    }
  };

  const handleCancelTransaction = async (transactionId) => {
    if (window.confirm('Are you sure you want to cancel this transaction? Notifications will be sent to both parties.')) {
      setLoading(true);
      try {
        await adminService.cancelTransaction(transactionId);
        setToastMessage('Transaction cancelled successfully');
        if (onRefresh) onRefresh();
      } catch (error) {
        setToastMessage(`Error: ${error.response?.data?.error?.message || 'Failed to cancel transaction'}`);
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="admin-page">
      <h2 className="page-title">Transaction Management</h2>
      <p className="page-subtitle">{filteredTransactions.length} total transactions</p>

      <div className="filter-tabs">
        {statuses.map((status) => (
          <button
            key={status}
            className={`filter-tab ${selectedStatus === status ? 'active' : ''}`}
            onClick={() => handleFilterChange(status)}
          >
            {status}
            {status !== 'All' && <span className="badge">{transactions.filter(t => t.status === status).length}</span>}
          </button>
        ))}
      </div>

      <div className="admin-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>TXN ID</th>
              <th>Book Title</th>
              <th>Owner</th>
              <th>Renter/Buyer</th>
              <th>Type</th>
              <th>Status</th>
              <th>Dates</th>
              <th>Amount</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredTransactions.length > 0 ? (
              filteredTransactions.map((txn) => (
                <tr key={txn.transactionId}>
                  <td className="mono">{txn.transactionId?.substring(0, 8)}</td>
                  <td>{txn.bookTitle || 'N/A'}</td>
                  <td className="text-small">{txn.ownerUsername || 'N/A'}</td>
                  <td className="text-small">{txn.renterUsername || 'N/A'}</td>
                  <td>
                    <span className="badge-type">
                      {txn.startDate && txn.endDate ? 'Rent' : 'Sale'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge-status status-${(txn.status || 'unknown').toLowerCase()}`}>
                      {txn.status}
                    </span>
                  </td>
                  <td className="text-muted text-small">
                    {txn.startDate && new Date(txn.startDate).toLocaleDateString()}
                    {txn.endDate && ` - ${new Date(txn.endDate).toLocaleDateString()}`}
                  </td>
                  <td className="text-right">${'amount' in txn ? txn.amount : 'N/A'}</td>
                  <td className="actions">
                    <button className="btn-small btn-primary" onClick={() => setSelectedTransaction(txn)}>
                      View Detail
                    </button>
                    {txn.status !== 'Completed' && txn.status !== 'Cancelled' && (
                      <button 
                        className="btn-small btn-danger" 
                        onClick={() => handleCancelTransaction(txn.transactionId)}
                        disabled={loading}
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="9" className="text-center text-muted">No transactions found</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Transaction Detail Modal */}
      {selectedTransaction && (
        <div className="modal-overlay" onClick={() => setSelectedTransaction(null)}>
          <div className="modal-content modal-large" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Transaction Detail</h3>
              <button className="close-btn" onClick={() => setSelectedTransaction(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="transaction-details-grid">
                <div className="detail-section">
                  <h4>Transaction Information</h4>
                  <p><strong>Transaction ID:</strong> {selectedTransaction.transactionId}</p>
                  <p><strong>Book ID:</strong> {selectedTransaction.bookId?.substring(0, 8)}</p>
                  <p><strong>Book Title:</strong> {selectedTransaction.bookTitle}</p>
                  <p><strong>Status:</strong> <span className={`badge-status status-${(selectedTransaction.status || 'unknown').toLowerCase()}`}>{selectedTransaction.status}</span></p>
                  <p><strong>Created:</strong> {new Date(selectedTransaction.createdAt).toLocaleString()}</p>
                </div>

                <div className="detail-section">
                  <h4>Parties Involved</h4>
                  <p><strong>Owner/Seller:</strong> {selectedTransaction.ownerUsername}</p>
                  <p className="text-muted text-small">ID: {selectedTransaction.ownerId?.substring(0, 8)}</p>
                  <p><strong>Renter/Buyer:</strong> {selectedTransaction.renterUsername}</p>
                  <p className="text-muted text-small">ID: {selectedTransaction.userId?.substring(0, 8)}</p>
                </div>

                <div className="detail-section">
                  <h4>Rental/Sale Details</h4>
                  {selectedTransaction.startDate && selectedTransaction.endDate ? (
                    <>
                      <p><strong>Start Date:</strong> {new Date(selectedTransaction.startDate).toLocaleDateString()}</p>
                      <p><strong>End Date:</strong> {new Date(selectedTransaction.endDate).toLocaleDateString()}</p>
                      <p><strong>Duration:</strong> {Math.ceil((new Date(selectedTransaction.endDate) - new Date(selectedTransaction.startDate)) / (1000 * 60 * 60 * 24))} days</p>
                    </>
                  ) : (
                    <p><strong>Type:</strong> Sale (one-time transaction)</p>
                  )}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelectedTransaction(null)}>Close</button>
              {selectedTransaction.status !== 'Completed' && selectedTransaction.status !== 'Cancelled' && (
                <button 
                  className="btn btn-danger" 
                  onClick={() => {
                    handleCancelTransaction(selectedTransaction.transactionId);
                    setSelectedTransaction(null);
                  }}
                  disabled={loading}
                >
                  {loading ? 'Cancelling...' : 'Cancel Transaction'}
                </button>
              )}
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
