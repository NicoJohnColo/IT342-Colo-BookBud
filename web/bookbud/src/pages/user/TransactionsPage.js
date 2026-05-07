import React, { useMemo, useState } from 'react';
import './TransactionsPage.css';
import userService from '../../services/userService';

const TABS = ['All', 'Pending', 'Active', 'Completed', 'Cancelled'];

const getPaymentStatusColor = (status) => {
  if (!status) return 'pending';
  const lower = String(status).toLowerCase();
  if (lower === 'successful' || lower === 'success') return 'success';
  if (lower === 'failed') return 'failed';
  return 'pending';
};

const formatPaymentStatus = (status) => {
  if (!status) return 'PENDING';
  return String(status).toUpperCase();
};

const formatCurrency = (amount) => {
  return `PHP ${Number(amount || 0).toFixed(2)}`;
};

export default function TransactionsPage({ transactions = [], onUpdateStatus, onSubmitRating }) {
  const [tab, setTab] = useState('All');
  const [processingId, setProcessingId] = useState('');
  const [ratingSubmittingId, setRatingSubmittingId] = useState('');
  const [ratingByTxn, setRatingByTxn] = useState({});
  const [selectedDetailUser, setSelectedDetailUser] = useState(null);
  const [userProfile, setUserProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [profileError, setProfileError] = useState('');

  const filtered = useMemo(() => {
    if (tab === 'All') return transactions;
    return transactions.filter((t) => String(t.status || '').toLowerCase() === tab.toLowerCase());
  }, [transactions, tab]);

  const countFor = (name) => {
    if (name === 'All') return transactions.length;
    return transactions.filter((t) => String(t.status || '').toLowerCase() === name.toLowerCase()).length;
  };

  const onAction = async (transactionId, status) => {
    setProcessingId(transactionId);
    try {
      await onUpdateStatus?.(transactionId, status);
    } catch (error) {
      const message =
        error?.response?.data?.error?.message ||
        error?.response?.data?.message ||
        'Could not update transaction status.';
      window.alert(message);
    } finally {
      setProcessingId('');
    }
  };

  const onCancel = async (transactionId) => {
    if (!window.confirm('Cancel this transaction?')) return;
    await onAction(transactionId, 'Cancelled');
  };

  const onRatingSubmit = async (transactionId) => {
    const ratingValue = Number(ratingByTxn[transactionId] || 5);
    if (!ratingValue || ratingValue < 1 || ratingValue > 5) {
      window.alert('Please choose a rating between 1 and 5.');
      return;
    }

    setRatingSubmittingId(transactionId);
    try {
      await onSubmitRating?.(transactionId, ratingValue);
    } catch (error) {
      const message =
        error?.response?.data?.error?.message ||
        error?.response?.data?.message ||
        'Could not submit rating.';
      window.alert(message);
    } finally {
      setRatingSubmittingId('');
    }
  };

  const handleViewDetails = async (userId, userName, isOwner) => {
    setSelectedDetailUser({ userId, userName, isOwner });
    setLoadingProfile(true);
    setProfileError('');
    setUserProfile(null);

    try {
      const profile = await userService.getUserProfile(userId);
      if (profile) {
        setUserProfile(profile);
      } else {
        setProfileError('Could not load user profile');
      }
    } catch (error) {
      console.error('Error fetching user profile:', error);
      setProfileError(error?.response?.data?.error?.message || 'Failed to load contact details');
    } finally {
      setLoadingProfile(false);
    }
  };

  const closeDetailModal = () => {
    setSelectedDetailUser(null);
    setUserProfile(null);
    setProfileError('');
  };

  return (
    <div>
      <h2 className="page-title">My Transactions</h2>
      <p className="page-subtitle">All your activity as buyer/renter and seller/lender</p>

      <div className="tabs">
        {TABS.map((name) => (
          <button key={name} className={`tab ${tab === name ? 'active' : ''}`} onClick={() => setTab(name)}>
            {name} <span className="tab-count">{countFor(name)}</span>
          </button>
        ))}
      </div>

      {!filtered.length && <div className="empty-state">No transactions here.</div>}

      <div className="txn-list">
        {filtered.map((txn) => {
          const status = String(txn.status || '').toLowerCase();
          const isLister = String(txn.userRole || '').toLowerCase() === 'owner';
          const isBusy = processingId === txn.transactionId;
          const canRate = status === 'completed' && (isLister ? !txn.ownerRated : !txn.renterRated);
          const ratingTargetLabel = isLister ? 'renter' : 'owner';
          const ratingValue = ratingByTxn[txn.transactionId] || 5;
          const isRatingBusy = ratingSubmittingId === txn.transactionId;

          return (
            <div key={txn.transactionId} className="txn-card">
              <div className="txn-header">
                <div>
                  <div className="txn-title">{txn.bookTitle || txn.transactionId}</div>
                  <div className="txn-meta">Lister: {txn.ownerUsername || txn.ownerId || 'N/A'} • Renter: {txn.renterUsername || txn.userId || 'N/A'}</div>
                </div>
                <span className={`status-badge ${String(txn.status || '').toLowerCase()}`}>{txn.status || 'pending'}</span>
              </div>

              <div className="txn-details">
                <div className="txn-detail-item">
                  <span className="txn-label">Dates:</span>
                  <span className="txn-value">{txn.startDate || 'N/A'} → {txn.endDate || 'N/A'}</span>
                </div>
                <div className="txn-detail-item">
                  <span className="txn-label">Amount:</span>
                  <span className="txn-value amount-highlight">{formatCurrency(txn.amount)}</span>
                </div>
                <div className="txn-detail-item">
                  <span className="txn-label">Payment Method:</span>
                  <span className="txn-value">{txn.paymentMethod || 'Not specified'}</span>
                </div>
                <div className="txn-detail-item">
                  <span className="txn-label">Payment Status:</span>
                  <span className={`payment-status ${getPaymentStatusColor(txn.paymentStatus)}`}>
                    {formatPaymentStatus(txn.paymentStatus)}
                  </span>
                </div>
                {txn.paymentDate && (
                  <div className="txn-detail-item">
                    <span className="txn-label">Payment Date:</span>
                    <span className="txn-value">{new Date(txn.paymentDate).toLocaleDateString()}</span>
                  </div>
                )}
                <div className="txn-detail-item">
                  <span className="txn-label">Transaction ID:</span>
                  <span className="txn-value txn-id">{txn.transactionId}</span>
                </div>
              </div>

                {isLister && status === 'pending' ? (
                  <div className="txn-actions">
                    <button className="btn btn-primary btn-sm" disabled={isBusy} onClick={() => onAction(txn.transactionId, 'Active')}>
                      {isBusy ? 'Updating...' : 'Approve Request'}
                    </button>
                    <button 
                      className="btn btn-secondary btn-sm" 
                      onClick={() => handleViewDetails(txn.userId, txn.renterUsername, false)}
                    >
                      View Buyer Details
                    </button>
                    <button className="btn btn-danger btn-sm" disabled={isBusy} onClick={() => onCancel(txn.transactionId)}>
                      Cancel Transaction
                    </button>
                  </div>
                ) : null}

                {isLister && status === 'active' ? (
                  <div className="txn-actions">
                    <button className="btn btn-primary btn-sm" disabled={isBusy} onClick={() => onAction(txn.transactionId, 'Completed')}>
                      {isBusy ? 'Updating...' : 'Confirm Payment'}
                    </button>
                    <button 
                      className="btn btn-secondary btn-sm" 
                      onClick={() => handleViewDetails(txn.userId, txn.renterUsername, false)}
                    >
                      Contact Buyer
                    </button>
                    <button className="btn btn-danger btn-sm" disabled={isBusy} onClick={() => onCancel(txn.transactionId)}>
                      Cancel Transaction
                    </button>
                  </div>
                ) : null}

                {!isLister && (status === 'pending' || status === 'active') ? (
                  <div className="txn-actions">
                    <button 
                      className="btn btn-secondary btn-sm" 
                      onClick={() => handleViewDetails(txn.ownerId, txn.ownerUsername, true)}
                    >
                      View Seller Details
                    </button>
                    <button className="btn btn-danger btn-sm" disabled={isBusy} onClick={() => onCancel(txn.transactionId)}>
                      Cancel Transaction
                    </button>
                  </div>
                ) : null}

                {canRate ? (
                  <div className="txn-rating">
                    <div className="txn-rating-label">Rate your {ratingTargetLabel}</div>
                    <div className="txn-rating-controls">
                      <select
                        className="txn-rating-select"
                        value={ratingValue}
                        onChange={(event) =>
                          setRatingByTxn((prev) => ({
                            ...prev,
                            [txn.transactionId]: Number(event.target.value),
                          }))
                        }
                      >
                        {[5, 4, 3, 2, 1].map((value) => (
                          <option key={value} value={value}>
                            {value} {value === 1 ? 'star' : 'stars'}
                          </option>
                        ))}
                      </select>
                      <button
                        className="btn btn-primary btn-sm"
                        disabled={isRatingBusy}
                        onClick={() => onRatingSubmit(txn.transactionId)}
                      >
                        {isRatingBusy ? 'Submitting...' : 'Submit Rating'}
                      </button>
                    </div>
                  </div>
                ) : status === 'completed' ? (
                  <div className="txn-rating-complete">Rating submitted</div>
                ) : null}
            </div>
          );
        })}
      </div>

    {/* Buyer/Seller Details Modal */}
    {selectedDetailUser && (
      <div className="modal-overlay" onClick={closeDetailModal}>
        <div className="modal-content modal-medium" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <h3>{selectedDetailUser.isOwner ? 'Seller' : 'Buyer'} Details</h3>
            <button className="close-btn" onClick={closeDetailModal}>×</button>
          </div>

          {loadingProfile ? (
            <div className="modal-body" style={{ textAlign: 'center', padding: '60px 20px' }}>
              <div className="loading-spinner"></div>
            </div>
          ) : profileError ? (
            <div className="modal-body" style={{ textAlign: 'center', color: '#dc2626', padding: '20px' }}>
              <p>{profileError}</p>
              <p style={{ fontSize: '12px', marginTop: '8px', color: '#666' }}>
                Unable to load {selectedDetailUser.isOwner ? 'seller' : 'buyer'} contact details. Make sure they have updated their profile.
              </p>
            </div>
          ) : userProfile ? (
            <div className="modal-body">
              <div className="user-detail-card">
                <div className="user-detail-avatar">
                  {(userProfile.username || selectedDetailUser.userName || 'U').charAt(0).toUpperCase()}
                </div>
                <div className="user-detail-info">
                  <h4>{userProfile.username || selectedDetailUser.userName}</h4>
                  <p className="user-rating">Rating: {userProfile.rating ? parseFloat(userProfile.rating).toFixed(2) : 'No rating'} ⭐</p>
                  <p className="user-joined">Member since: {userProfile.createdAt ? new Date(userProfile.createdAt).toLocaleDateString() : 'N/A'}</p>
                </div>
              </div>

              <div className="contact-details-section">
                <h5 className="section-heading">Contact Information</h5>
                
                <div className="contact-item">
                  <span className="contact-label">📱 Mobile:</span>
                  <span className="contact-value">
                    {userProfile.mobileNumber ? (
                      <a href={`tel:${userProfile.mobileNumber}`} className="contact-link">
                        {userProfile.mobileNumber}
                      </a>
                    ) : (
                      <span className="contact-unavailable">Not provided</span>
                    )}
                  </span>
                </div>

                <div className="contact-item">
                  <span className="contact-label">💬 Messenger:</span>
                  <span className="contact-value">
                    {userProfile.messenger ? (
                      <span className="contact-text">{userProfile.messenger}</span>
                    ) : (
                      <span className="contact-unavailable">Not provided</span>
                    )}
                  </span>
                </div>

                <div className="contact-item">
                  <span className="contact-label">f Facebook:</span>
                  <span className="contact-value">
                    {userProfile.facebookUrl ? (
                      <a href={userProfile.facebookUrl} target="_blank" rel="noopener noreferrer" className="contact-link">
                        Visit Profile
                      </a>
                    ) : (
                      <span className="contact-unavailable">Not provided</span>
                    )}
                  </span>
                </div>
              </div>

              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={closeDetailModal}>Close</button>
              </div>
            </div>
          ) : (
            <div className="modal-body" style={{ textAlign: 'center', padding: '40px 20px' }}>
              <p style={{ color: '#666' }}>No details available</p>
            </div>
          )}
        </div>
      </div>
    )}
    </div>
  );
}
