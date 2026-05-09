import React from 'react';
import './NotificationsPage.css';

export default function NotificationsPage({ notifications = [], onMarkRead, onMarkAllRead, onDelete }) {
  const unread = notifications.filter((n) => !n.isRead).length;

  return (
    <div>
      <div className="top-bar">
        <div>
          <h2 className="page-title">Notifications</h2>
          <p className="page-subtitle">{unread} unread</p>
        </div>
        {unread > 0 && <button className="btn btn-outline btn-sm" onClick={onMarkAllRead}>Mark All Read</button>}
      </div>

      {!notifications.length && <div className="empty-state">No notifications.</div>}

      <div className="notification-list">
        {notifications.map((n) => (
          <div key={n.notificationId} className={`notification-item ${!n.isRead ? 'unread' : ''}`}>
            <div className="notification-main" onClick={() => onMarkRead(n.notificationId)}>
              <div className="notification-text">{n.message}</div>
              <div className="notification-time">{n.createdAt || ''}</div>
            </div>
            <div className="notification-side">
              <span className={`status-badge ${n.isRead ? 'completed' : 'pending'}`}>{n.isRead ? 'Read' : 'Unread'}</span>
              <button className="btn btn-ghost btn-sm" onClick={() => onDelete(n.notificationId)}>Dismiss</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
