import React, { useState } from 'react';
import './AdminPages.css';
import '../user/NotificationsPage.css';

export default function AdminNotificationsPage({ notifications = [] }) {
  const [filteredNotifications, setFilteredNotifications] = useState(notifications);
  const [selectedFilter, setSelectedFilter] = useState('All');

  const filters = ['All', 'Unread', 'Read'];

  const handleFilterChange = (filter) => {
    setSelectedFilter(filter);
    if (filter === 'All') {
      setFilteredNotifications(notifications);
    } else if (filter === 'Unread') {
      setFilteredNotifications(notifications.filter(n => !n.isRead));
    } else {
      setFilteredNotifications(notifications.filter(n => n.isRead));
    }
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div className="admin-page">
      <div className="top-bar">
        <div>
          <h2 className="page-title">Notification Logs</h2>
          <p className="page-subtitle">{filteredNotifications.length} notifications ({unreadCount} unread)</p>
        </div>
      </div>

      <div className="filter-tabs">
        {filters.map((filter) => (
          <button
            key={filter}
            className={`filter-tab ${selectedFilter === filter ? 'active' : ''}`}
            onClick={() => handleFilterChange(filter)}
          >
            {filter}
            {filter === 'Unread' && <span className="badge">{unreadCount}</span>}
            {filter === 'Read' && <span className="badge">{notifications.filter(n => n.isRead).length}</span>}
          </button>
        ))}
      </div>

      {!filteredNotifications.length ? (
        <div className="empty-state">No notifications found.</div>
      ) : (
        <div className="notification-list">
          {filteredNotifications.map((n) => (
            <div key={n.notificationId} className={`notification-item ${!n.isRead ? 'unread' : ''}`}>
              <div className="notification-indicator">
                <span className={`indicator ${!n.isRead ? 'unread' : 'read'}`}></span>
              </div>
              <div className="notification-main readonly">
                <div className="notification-text">{n.message}</div>
                <div className="notification-meta">
                  User: {n.userId?.substring(0, 8) || 'System'} • ID: {n.notificationId?.substring(0, 8)}
                </div>
                <div className="notification-time">{new Date(n.createdAt).toLocaleString()}</div>
              </div>
              <div className="notification-side">
                <span className={`status-badge ${n.isRead ? 'read' : 'unread'}`}>
                  {n.isRead ? '✓ Read' : '● Unread'}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
