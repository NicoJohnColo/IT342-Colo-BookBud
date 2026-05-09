import React, { useState } from 'react';
import adminService from '../../services/adminService';
import './AdminPages.css';

export default function AdminUsersPage({ users = [], onRefresh }) {
  const [filteredUsers, setFilteredUsers] = useState(users);
  const [selectedStatus, setSelectedStatus] = useState('All');
  const [loading, setLoading] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [statusModal, setStatusModal] = useState(null);

  const statuses = ['All', 'Active', 'Suspended', 'Banned'];

  const handleFilterChange = (status) => {
    setSelectedStatus(status);
    if (status === 'All') {
      setFilteredUsers(users);
    } else {
      setFilteredUsers(users.filter(u => u.accountStatus === status));
    }
  };

  const handleStatusChange = async (userId, newStatus) => {
    setLoading(true);
    try {
      await adminService.updateUserStatus(userId, newStatus);
      setToastMessage(`User status updated to ${newStatus}`);
      setStatusModal(null);
      if (onRefresh) onRefresh();
    } catch (error) {
      setToastMessage(`Error: ${error.response?.data?.error?.message || 'Failed to update status'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-page">
      <h2 className="page-title">User Management</h2>
      <p className="page-subtitle">{filteredUsers.length} registered users</p>

      <div className="filter-tabs">
        {statuses.map((status) => (
          <button
            key={status}
            className={`filter-tab ${selectedStatus === status ? 'active' : ''}`}
            onClick={() => handleFilterChange(status)}
          >
            {status}
            {status !== 'All' && <span className="badge">{users.filter(u => u.accountStatus === status).length}</span>}
          </button>
        ))}
      </div>

      <div className="admin-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>User ID</th>
              <th>Username</th>
              <th>Email</th>
              <th>Role</th>
              <th>Rating</th>
              <th>Status</th>
              <th>Registered</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.length > 0 ? (
              filteredUsers.map((user) => (
                <tr key={user.userId}>
                  <td className="mono">{user.userId?.substring(0, 8)}</td>
                  <td>{user.username}</td>
                  <td className="text-muted text-small">{user.email || 'N/A'}</td>
                  <td><span className="badge-role">{user.role || 'USER'}</span></td>
                  <td>
                    {user.rating ? (
                      <span className="rating">
                        {'★'.repeat(Math.floor(user.rating))}
                        {'☆'.repeat(5 - Math.floor(user.rating))} {user.rating.toFixed(1)}
                      </span>
                    ) : 'N/A'}
                  </td>
                  <td>
                    <span className={`badge-status status-${(user.accountStatus || 'active').toLowerCase()}`}>
                      {user.accountStatus || 'Active'}
                    </span>
                  </td>
                  <td className="text-muted">{new Date(user.createdAt).toLocaleDateString()}</td>
                  <td className="actions">
                    <button className="btn-small btn-primary" onClick={() => setSelectedUser(user)}>
                      View Profile
                    </button>
                    <button 
                      className="btn-small btn-warning" 
                      onClick={() => setStatusModal(user)}
                      disabled={(user.accountStatus || 'Active') === (user.role === 'ADMIN' ? 'Active' : 'Active')}
                    >
                      {(user.accountStatus === 'Suspended') ? 'Restore' : 'Suspend'}
                    </button>
                    {(user.accountStatus !== 'Banned') && (
                      <button className="btn-small btn-danger" onClick={() => setStatusModal({ ...user, shouldBan: true })}>
                        Ban
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr><td colSpan="8" className="text-center text-muted">No users found</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* User Detail Modal */}
      {selectedUser && (
        <div className="modal-overlay" onClick={() => setSelectedUser(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>User Profile</h3>
              <button className="close-btn" onClick={() => setSelectedUser(null)}>×</button>
            </div>
            <div className="modal-body">
              <p><strong>User ID:</strong> {selectedUser.userId}</p>
              <p><strong>Username:</strong> {selectedUser.username}</p>
              <p><strong>Email:</strong> {selectedUser.email || 'N/A'}</p>
              <p><strong>Role:</strong> {selectedUser.role || 'USER'}</p>
              <p><strong>Rating:</strong> {selectedUser.rating ? selectedUser.rating.toFixed(2) : 'No rating'}</p>
              <p><strong>Account Status:</strong> {selectedUser.accountStatus || 'Active'}</p>
              <p><strong>Registered:</strong> {new Date(selectedUser.createdAt).toLocaleString()}</p>
              <p className="text-muted text-small">Contact details: facebook_url, messenger, mobile_number (private)</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelectedUser(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* Status Update Modal */}
      {statusModal && (
        <div className="modal-overlay" onClick={() => setStatusModal(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Update User Status</h3>
              <button className="close-btn" onClick={() => setStatusModal(null)}>×</button>
            </div>
            <div className="modal-body">
              <p>Change status for: <strong>{statusModal.username}</strong></p>
              <p>Current Status: <strong>{statusModal.accountStatus || 'Active'}</strong></p>
              <p>New Status:</p>
              <select className="form-control" defaultValue={statusModal.accountStatus || 'Active'}>
                <option value="Active">Active</option>
                <option value="Suspended">Suspended</option>
                <option value="Banned">Banned</option>
              </select>
              {statusModal.shouldBan && (
                <p className="warning-text">⚠️ Banned users cannot log in.</p>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setStatusModal(null)}>Cancel</button>
              <button 
                className="btn btn-primary" 
                onClick={() => {
                  const select = document.querySelector('.modal-content select');
                  handleStatusChange(statusModal.userId, select.value);
                }}
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
