import React from 'react';
import './AdminPages.css';

export default function AdminDashboardPage({ 
  books = [], 
  users = [], 
  transactions = [], 
  notifications = [],
  onNavigate = () => {}
}) {
  const activeTransactions = transactions.filter((t) => String(t.status || '').toLowerCase() === 'active').length;
  const pendingTransactions = transactions.filter((t) => String(t.status || '').toLowerCase() === 'pending').length;
  const completedTransactions = transactions.filter((t) => String(t.status || '').toLowerCase() === 'completed').length;
  const unreadNotifications = notifications.filter((n) => !n.isRead).length;
  const flaggedListings = books.filter((b) => b.status === 'Unavailable').length;
  const suspendedUsers = users.filter((u) => u.accountStatus === 'Suspended' || u.accountStatus === 'Banned').length;

  return (
    <div className="admin-page">
      <h2 className="page-title">Admin Dashboard</h2>
      <p className="page-subtitle">BookBud platform overview and quick access</p>

      {/* KPI Stats Grid */}
      <div className="admin-grid">
        <div className="admin-stat">
          <div className="admin-stat-label">Total Users</div>
          <div className="admin-stat-value">{users.length}</div>
          <div className="admin-stat-subtitle text-muted text-small">{suspendedUsers} suspended/banned</div>
        </div>
        
        <div className="admin-stat">
          <div className="admin-stat-label">Total Listings</div>
          <div className="admin-stat-value">{books.length}</div>
          <div className="admin-stat-subtitle text-muted text-small">{flaggedListings} flagged</div>
        </div>
        
        <div className="admin-stat">
          <div className="admin-stat-label">Active Transactions</div>
          <div className="admin-stat-value">{activeTransactions}</div>
          <div className="admin-stat-subtitle text-muted text-small">{pendingTransactions} pending</div>
        </div>
        
        <div className="admin-stat">
          <div className="admin-stat-label">Unread Notifications</div>
          <div className="admin-stat-value">{unreadNotifications}</div>
          <div className="admin-stat-subtitle text-muted text-small">of {notifications.length} total</div>
        </div>
      </div>

      {/* Quick Navigation Cards */}
      <div className="quick-nav-section">
        <h3 className="section-title">Quick Navigation</h3>
        <div className="quick-nav-grid">
          <div className="nav-card" onClick={() => onNavigate('admin-books')} role="button" tabIndex={0}>
            <div className="nav-card-icon">📚</div>
            <h4>Book Management</h4>
            <p className="text-muted">Review • edit • remove listings</p>
            <div className="nav-card-stat">{flaggedListings} flagged for review</div>
          </div>

          <div className="nav-card" onClick={() => onNavigate('admin-users')} role="button" tabIndex={0}>
            <div className="nav-card-icon">👥</div>
            <h4>User Management</h4>
            <p className="text-muted">View • suspend • restore accounts</p>
            <div className="nav-card-stat">{suspendedUsers} accounts restricted</div>
          </div>

          <div className="nav-card" onClick={() => onNavigate('admin-transactions')} role="button" tabIndex={0}>
            <div className="nav-card-icon">💱</div>
            <h4>Transaction Management</h4>
            <p className="text-muted">Monitor • cancel disputed transactions</p>
            <div className="nav-card-stat">{activeTransactions} active now</div>
          </div>

          <div className="nav-card" onClick={() => onNavigate('admin-notifications')} role="button" tabIndex={0}>
            <div className="nav-card-icon">🔔</div>
            <h4>Notification Logs</h4>
            <p className="text-muted">View system-wide notification log</p>
            <div className="nav-card-stat">{unreadNotifications} unread messages</div>
          </div>
        </div>
      </div>

      {/* Recent Activity Section */}
      <div className="activity-section">
        <h3 className="section-title">Recent Transactions</h3>
        {transactions.length > 0 ? (
          <div className="recent-table-wrap">
            <table className="data-table compact">
              <thead>
                <tr>
                  <th>Book</th>
                  <th>Owner → Renter</th>
                  <th>Status</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {transactions.slice(0, 5).map((txn) => (
                  <tr key={txn.transactionId}>
                    <td className="text-small">{txn.bookTitle || 'N/A'}</td>
                    <td className="text-small">
                      {txn.ownerUsername || 'N/A'} → {txn.renterUsername || 'N/A'}
                    </td>
                    <td>
                      <span className={`badge-status status-${(txn.status || 'unknown').toLowerCase()}`}>
                        {txn.status}
                      </span>
                    </td>
                    <td className="text-muted text-small">
                      {new Date(txn.createdAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state text-muted">No recent transactions</div>
        )}
      </div>

      {/* Alerts Section */}
      {(suspendedUsers > 0 || flaggedListings > 0) && (
        <div className="alerts-section">
          <h3 className="section-title">Alerts</h3>
          <div className="alerts-list">
            {suspendedUsers > 0 && (
              <div className="alert alert-warning">
                <span className="alert-icon">⚠️</span>
                <div>
                  <strong>{suspendedUsers} user account{suspendedUsers !== 1 ? 's' : ''}</strong> suspended or banned pending review
                </div>
              </div>
            )}
            {flaggedListings > 0 && (
              <div className="alert alert-info">
                <span className="alert-icon">ℹ️</span>
                <div>
                  <strong>{flaggedListings} book listing{flaggedListings !== 1 ? 's' : ''}</strong> marked unavailable or flagged
                </div>
              </div>
            )}
            {unreadNotifications > 0 && (
              <div className="alert alert-info">
                <span className="alert-icon">🔔</span>
                <div>
                  <strong>{unreadNotifications} unread notification{unreadNotifications !== 1 ? 's' : ''}</strong> waiting review
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
