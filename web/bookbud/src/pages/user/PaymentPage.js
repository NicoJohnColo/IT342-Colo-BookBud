import React, { useMemo, useState, useEffect } from 'react';
import './PaymentPage.css';

const PAYMENT_TABS = ['All', 'Pending', 'Successful', 'Failed'];

const getPaymentStatusColor = (status) => {
  if (!status) return 'pending';
  const lower = String(status).toLowerCase();
  if (lower === 'successful' || lower === 'success' || lower === 'completed') return 'success';
  if (lower === 'failed' || lower === 'cancelled') return 'failed';
  if (lower === 'active') return 'success';
  return 'pending';
};

const formatPaymentStatus = (status) => {
  if (!status) return 'PENDING';
  const lower = String(status).toLowerCase();
  if (lower === 'completed' || lower === 'active') return 'SUCCESSFUL';
  return String(status).toUpperCase();
};

const formatCurrency = (amount) => {
  return `PHP ${Number(amount || 0).toFixed(2)}`;
};

export default function PaymentPage({ transactions = [], books = [] }) {
  const [tab, setTab] = useState('All');

  // Create a map of books by ID for quick lookup
  const bookMap = useMemo(() => {
    const map = {};
    books.forEach((book) => {
      map[book.bookId] = book;
    });
    return map;
  }, [books]);

  // Transform transactions into payment records
  const payments = useMemo(() => {
    console.log('Transaction data:', transactions); // Debug: see what data we're getting
    console.log('Books available:', books.length); // Debug
    
    return transactions.map((txn) => {
      // Get the book to find its price
      const book = bookMap[txn.bookId];
      
      // Determine the price based on transaction type
      let amount = 0;
      if (book) {
        if (String(txn.type || '').toUpperCase() === 'RENT') {
          amount = book.priceRent || book.rentalPrice || 0;
        } else {
          amount = book.priceSale || book.salePrice || book.price || 0;
        }
      }
      
      // Fallback to transaction amount if available
      amount = amount || txn.amount || txn.transactionAmount || 0;
      
      console.log('Transaction amount:', amount, 'Book:', book); // Debug
      
      return {
        paymentId: txn.transactionId,
        transactionId: txn.transactionId,
        amount: Number(amount) || 0,
        paymentMethod: txn.paymentMethod || txn.method || 'Cash',
        status: txn.paymentStatus || txn.status || 'pending',
        paymentDate: txn.paymentDate || txn.updatedAt || txn.createdAt,
        type: String(txn.type || '').toUpperCase() === 'RENT' ? 'Rental' : 'Purchase',
        transactionType: txn.type,
        bookTitle: txn.bookTitle || book?.title || 'Book',
        otherPartyName: txn.ownerUsername || txn.renterUsername || txn.userName || 'User',
        createdAt: txn.createdAt,
      };
    });
  }, [transactions, bookMap]);

  // Filter payments by tab
  const filtered = useMemo(() => {
    if (tab === 'All') return payments;
    return payments.filter((p) => {
      const status = String(p.status || '').toLowerCase();
      if (tab === 'Pending') return status === 'pending';
      if (tab === 'Successful') return status === 'completed' || status === 'active' || status === 'successful';
      if (tab === 'Failed') return status === 'cancelled' || status === 'failed';
      return false;
    });
  }, [payments, tab]);

  // Calculate summaries
  const totalEarnings = useMemo(
    () => payments
      .filter((p) => {
        const status = String(p.status || '').toLowerCase();
        return status === 'completed' || status === 'active' || status === 'successful';
      })
      .reduce((sum, p) => sum + (p.amount || 0), 0),
    [payments]
  );

  const totalPending = useMemo(
    () => payments
      .filter((p) => String(p.status || '').toLowerCase() === 'pending')
      .reduce((sum, p) => sum + (p.amount || 0), 0),
    [payments]
  );

  const totalSuccessful = useMemo(
    () => payments
      .filter((p) => {
        const status = String(p.status || '').toLowerCase();
        return status === 'completed' || status === 'active' || status === 'successful';
      })
      .reduce((sum, p) => sum + (p.amount || 0), 0),
    [payments]
  );

  const countFor = (name) => {
    if (name === 'All') return payments.length;
    const lower = name.toLowerCase();
    if (lower === 'pending') return payments.filter((p) => String(p.status || '').toLowerCase() === 'pending').length;
    if (lower === 'successful') return payments.filter((p) => {
      const status = String(p.status || '').toLowerCase();
      return status === 'completed' || status === 'active' || status === 'successful';
    }).length;
    if (lower === 'failed') {
      return payments.filter((p) => {
        const status = String(p.status || '').toLowerCase();
        return status === 'cancelled' || status === 'failed';
      }).length;
    }
    return 0;
  };

  return (
    <div>
      <h2 className="page-title">Payment History & Earnings</h2>
      <p className="page-subtitle">Track all your payments and earnings</p>

      {/* Earnings Summary Cards */}
      <div className="payment-summary">
        <div className="summary-card">
          <div className="summary-label">Total Earnings</div>
          <div className="summary-value">{formatCurrency(totalEarnings)}</div>
          <div className="summary-description">From successful transactions</div>
        </div>

        <div className="summary-card warning">
          <div className="summary-label">Pending Payments</div>
          <div className="summary-value">{formatCurrency(totalPending)}</div>
          <div className="summary-description">Awaiting confirmation</div>
        </div>

        <div className="summary-card success">
          <div className="summary-label">Confirmed Payments</div>
          <div className="summary-value">{formatCurrency(totalSuccessful)}</div>
          <div className="summary-description">Successfully received</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        {PAYMENT_TABS.map((name) => {
          const count = name === 'All' ? payments.length :
            name === 'Pending' ? payments.filter((p) => String(p.status || '').toLowerCase() === 'pending').length :
            name === 'Successful' ? payments.filter((p) => {
              const status = String(p.status || '').toLowerCase();
              return status === 'completed' || status === 'active' || status === 'successful';
            }).length :
            payments.filter((p) => {
              const status = String(p.status || '').toLowerCase();
              return status === 'cancelled' || status === 'failed';
            }).length;
          
          return (
            <button key={name} className={`tab ${tab === name ? 'active' : ''}`} onClick={() => setTab(name)}>
              {name} <span className="tab-count">{count}</span>
            </button>
          );
        })}
      </div>

      {/* Payment List */}
      {!filtered.length && <div className="empty-state">No payments here.</div>}

      <div className="payment-list">
        {filtered.map((payment) => (
          <div key={payment.paymentId} className="payment-card">
            <div className="payment-header">
              <div className="payment-info">
                <div className="payment-title">
                  {payment.transactionType === 'RENT' ? '📦 Book Rental' : '🛒 Book Purchase'}
                </div>
                <div className="payment-meta">
                  <span>{payment.bookTitle}</span>
                  {payment.otherPartyName && <span>From/To: {payment.otherPartyName}</span>}
                  {payment.transactionId && <span>TXN: {String(payment.transactionId).slice(0, 8)}...</span>}
                </div>
              </div>
              <span className={`payment-status ${getPaymentStatusColor(payment.status)}`}>
                {formatPaymentStatus(payment.status)}
              </span>
            </div>

            <div className="payment-details">
              <div className="detail-row">
                <span className="detail-label">Amount:</span>
                <span className="detail-value amount">{formatCurrency(payment.amount)}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Method:</span>
                <span className="detail-value">{payment.paymentMethod || 'Not specified'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Type:</span>
                <span className="detail-value">{payment.type}</span>
              </div>
              {payment.paymentDate && (
                <div className="detail-row">
                  <span className="detail-label">Date:</span>
                  <span className="detail-value">{new Date(payment.paymentDate).toLocaleDateString()}</span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
