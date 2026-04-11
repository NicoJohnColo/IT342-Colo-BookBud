import React, { useMemo, useState } from 'react';
import './TransactionsPage.css';

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

export default function TransactionsPage({ transactions = [], onUpdateStatus }) {
  const [tab, setTab] = useState('All');
  const [processingId, setProcessingId] = useState('');

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
                  </div>
                ) : null}

                {isLister && status === 'active' ? (
                  <div className="txn-actions">
                    <button className="btn btn-primary btn-sm" disabled={isBusy} onClick={() => onAction(txn.transactionId, 'Completed')}>
                      {isBusy ? 'Updating...' : 'Confirm Payment'}
                    </button>
                  </div>
                ) : null}
            </div>
          );
        })}
      </div>
    </div>
  );
}
