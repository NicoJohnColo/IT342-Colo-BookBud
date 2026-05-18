import React, { useMemo, useState } from 'react';
import './PaymentPage.css';
import paymentService from '../services/paymentService';
import useEarnings from '../hooks/useEarnings';

const PAYMENT_TABS = ['All', 'Pending', 'Successful', 'Failed'];
const VIEW_MODES = ['Earnings', 'Spending'];

const getPaymentStatusColor = (status) => {
  if (!status) return 'pending';
  const lower = String(status).toLowerCase();
  if (lower === 'paid') return 'success';
  if (lower === 'failed') return 'failed';
  if (lower === 'pending') return 'pending';
  return 'pending';
};

const formatPaymentStatus = (status) => {
  if (!status) return 'PENDING';
  const lower = String(status).toLowerCase();
  if (lower === 'paid') return 'SUCCESSFUL';
  return String(status).toUpperCase();
};

const formatCurrency = (amount) => {
  return `PHP ${Number(amount || 0).toFixed(2)}`;
};

const formatPaymentMethod = (method) => {
  if (!method) return 'Cash';
  const lower = String(method).toLowerCase();
  if (lower === 'stripe_card') return 'Stripe (Card)';
  if (lower === 'bank_transfer') return 'Bank Transfer';
  return String(method).replace('_', ' ');
};

export default function PaymentPage({ transactions = [], books = [] }) {
  const [tab, setTab] = useState('All');
  const [viewMode, setViewMode] = useState('Earnings');
  const [payments, setPayments] = useState([]);
  const { earnings, loading, error, refresh } = useEarnings();

  // Use the shared hook to fetch earnings; manually load payments list
  React.useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const fetchMethod = viewMode === 'Earnings' 
          ? paymentService.getPaymentsReceived 
          : paymentService.getPaymentsMade;
          
        const paymentsData = await fetchMethod({ page: 0, size: 100 });
        if (!mounted) return;
        if (paymentsData?.content) setPayments(paymentsData.content);
        else if (Array.isArray(paymentsData)) setPayments(paymentsData);
        else setPayments([]);
      } catch (err) {
        console.warn(`Could not fetch ${viewMode.toLowerCase()}, showing empty list:`, err?.message || err);
        if (!mounted) return;
        setPayments([]);
      }
    })();
    return () => { mounted = false; };
  }, [refresh, viewMode]);

  // Transform and enrich payment records with transaction info
  const enrichedPayments = useMemo(() => {
    return payments.map((payment) => {
      // Find corresponding transaction for additional details
      const txn = transactions.find((t) => t.transactionId === payment.transactionId);
      
      // Use transaction amount, then payment amount, then 0
      const actualAmount = txn?.amount ?? payment.amount ?? 0;
      
      return {
        paymentId: payment.paymentId,
        transactionId: payment.transactionId,
        amount: Number(actualAmount) || 0,
        paymentMethod: payment.paymentMethod || 'Cash',
        status: payment.paymentStatus || 'Pending',
        paymentDate: payment.paymentDate,
        bookTitle: txn?.bookTitle || 'Book',
        otherPartyName: txn?.renterUsername || txn?.buyerUsername || 'User',
        type: txn?.type === 'RENT' ? 'Rental' : 'Purchase',
        transactionType: txn?.type || 'SALE',
      };
    });
  }, [payments, transactions]);

  // Filter payments by tab
  const filtered = useMemo(() => {
    if (tab === 'All') return enrichedPayments;
    return enrichedPayments.filter((p) => {
      const status = String(p.status || '').toLowerCase();
      if (tab === 'Pending') return status === 'pending';
      if (tab === 'Successful') return status === 'paid';
      if (tab === 'Failed') return status === 'failed';
      return false;
    });
  }, [enrichedPayments, tab]);

  const totalEarnings = earnings?.totalEarnings || 0;
  const pendingPaymentCount = earnings?.pendingPayments || 0;
  const successfulPaymentCount = earnings?.successfulPayments || 0;

  // Handler to mark payment as received (Paid)
  const handleMarkAsReceived = async (paymentId) => {
    try {
      await paymentService.updatePaymentStatus(paymentId, 'Paid');
      // Refresh earnings and payments via shared hook / loader
      refresh();
      const paymentsData = await paymentService.getPaymentsReceived({ page: 0, size: 100 });
      if (paymentsData?.content) setPayments(paymentsData.content);
      else if (Array.isArray(paymentsData)) setPayments(paymentsData);
    } catch (error) {
      console.error('Error marking payment as received:', error);
      alert('Failed to confirm payment. Please try again.');
    }
  };

  const countFor = (name) => {
    if (name === 'All') return enrichedPayments.length;
    const lower = name.toLowerCase();
    if (lower === 'pending') return enrichedPayments.filter((p) => String(p.status || '').toLowerCase() === 'pending').length;
    if (lower === 'successful') return enrichedPayments.filter((p) => String(p.status || '').toLowerCase() === 'paid').length;
    if (lower === 'failed') return enrichedPayments.filter((p) => String(p.status || '').toLowerCase() === 'failed').length;
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
          <div className="summary-value">{pendingPaymentCount}</div>
          <div className="summary-description">Awaiting confirmation</div>
        </div>

        <div className="summary-card success">
          <div className="summary-label">Confirmed Payments</div>
          <div className="summary-value">{successfulPaymentCount}</div>
          <div className="summary-description">Successfully received</div>
        </div>
      </div>

      {/* View Mode Toggle */}
      <div className="view-mode-tabs" style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
        {VIEW_MODES.map(mode => (
          <button 
            key={mode} 
            className={`view-mode-btn ${viewMode === mode ? 'active' : ''}`}
            style={{
              padding: '10px 20px',
              borderRadius: '8px',
              border: '1px solid #e5e7eb',
              backgroundColor: viewMode === mode ? '#f97316' : '#fff',
              color: viewMode === mode ? '#fff' : '#4b5563',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s'
            }}
            onClick={() => { setViewMode(mode); setTab('All'); }}
          >
            {mode === 'Earnings' ? '💰 Earnings (As Seller)' : '🛒 Spending (As Buyer)'}
          </button>
        ))}
      </div>

      {/* Tabs */}
      <div className="tabs">
        {PAYMENT_TABS.map((name) => {
          const count = countFor(name);
          return (
            <button key={name} className={`tab ${tab === name ? 'active' : ''}`} onClick={() => setTab(name)}>
              {name} <span className="tab-count">{count}</span>
            </button>
          );
        })}
      </div>

      {/* Payment List */}
      {loading && <div className="loading-state">Loading payments...</div>}
      {!loading && !filtered.length && <div className="empty-state">No payments here.</div>}

      <div className="payment-list">
        {filtered.map((payment) => (
          <div key={payment.paymentId} className="payment-card">
            <div className="payment-header">
              <div className="payment-info">
                <div className="payment-title">
                  {payment.type === 'Rental' ? '📦 Book Rental' : '🛒 Book Purchase'}
                </div>
                <div className="payment-meta">
                  <span>{payment.bookTitle}</span>
                  {payment.otherPartyName && <span>From: {payment.otherPartyName}</span>}
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
                <span className="detail-label">Payment Method:</span>
                <span className="detail-value">{formatPaymentMethod(payment.paymentMethod)}</span>
              </div>
              {payment.paymentDate && (
                <div className="detail-row">
                  <span className="detail-label">Date:</span>
                  <span className="detail-value">{new Date(payment.paymentDate).toLocaleDateString()}</span>
                </div>
              )}
            </div>

            {/* Action button for pending payments */}
            {String(payment.status || '').toLowerCase() === 'pending' && (
              <div className="payment-actions">
                <button
                  className="btn-confirm-payment"
                  onClick={() => handleMarkAsReceived(payment.paymentId)}
                >
                  ✅ Mark as Received
                </button>
                <span className="payment-hint">Click to confirm you received this payment</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

