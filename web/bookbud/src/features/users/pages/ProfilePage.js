import React, { useMemo, useState } from 'react';
import './ProfilePage.css';
import { FaPencilAlt, FaSave, FaTimes } from 'react-icons/fa';

export default function ProfilePage({ user, profile, myListingsCount, transactionsCount, onUpdateProfile }) {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    username: profile?.username || user?.username || '',
    email: user?.email || '',
    facebookUrl: profile?.facebookUrl || '',
    messenger: profile?.messenger || '',
    mobileNumber: profile?.mobileNumber || '',
  });
  const [saving, setSaving] = useState(false);

  const joined = useMemo(() => {
    if (!profile?.createdAt && !user?.createdAt) return 'N/A';
    const value = profile?.createdAt || user?.createdAt;
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleDateString();
  }, [profile, user]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await onUpdateProfile?.(formData);
      setIsEditing(false);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setFormData({
      username: profile?.username || user?.username || '',
      email: user?.email || '',
      facebookUrl: profile?.facebookUrl || '',
      messenger: profile?.messenger || '',
      mobileNumber: profile?.mobileNumber || '',
    });
    setIsEditing(false);
  };

  return (
    <div>
      <div className="profile-header-row">
        <div>
          <h2 className="page-title">Profile</h2>
          <p className="page-subtitle">Manage your account info and contact details</p>
        </div>
        {!isEditing ? (
          <button className="btn btn-primary" onClick={() => setIsEditing(true)}>
            <FaPencilAlt style={{ marginRight: 6 }} />
            Edit Profile
          </button>
        ) : (
          <div className="profile-edit-actions">
            <button className="btn btn-secondary" onClick={handleCancel} disabled={saving}>
              <FaTimes style={{ marginRight: 6 }} />
              Cancel
            </button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
              <FaSave style={{ marginRight: 6 }} />
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        )}
      </div>

      <div className="profile-grid">
        <div className="profile-card">
          <div className="profile-avatar">{(profile?.username || user?.username || 'U').slice(0, 1).toUpperCase()}</div>
          <div className="profile-name">{profile?.username || user?.username || 'User'}</div>
          <div className="profile-rating">Rating: {profile?.rating || user?.rating || 'N/A'}</div>
          <div className="profile-stat-row"><span>Listings Published</span><strong>{myListingsCount}</strong></div>
          <div className="profile-stat-row"><span>Transactions</span><strong>{transactionsCount}</strong></div>
          <div className="profile-stat-row"><span>Member Since</span><strong>{joined}</strong></div>
        </div>

        <div className="profile-info-card">
          <div className="profile-section-title">Account Information</div>
          {isEditing ? (
            <>
              <div className="profile-field-edit">
                <label>Username</label>
                <input type="text" name="username" value={formData.username} onChange={handleChange} />
              </div>
              <div className="profile-field-edit">
                <label>Email</label>
                <input type="email" name="email" value={formData.email} onChange={handleChange} />
              </div>
            </>
          ) : (
            <>
              <div className="profile-field"><strong>Username:</strong> {profile?.username || user?.username || 'N/A'}</div>
              <div className="profile-field"><strong>Email:</strong> {user?.email || 'N/A'}</div>
            </>
          )}

          <div className="profile-section-title contact-title">Contact Information</div>
          {isEditing ? (
            <>
              <div className="profile-field-edit">
                <label>Facebook URL</label>
                <input type="text" name="facebookUrl" value={formData.facebookUrl} onChange={handleChange} placeholder="https://facebook.com/..." />
              </div>
              <div className="profile-field-edit">
                <label>Messenger</label>
                <input type="text" name="messenger" value={formData.messenger} onChange={handleChange} />
              </div>
              <div className="profile-field-edit">
                <label>Mobile Number</label>
                <input type="tel" name="mobileNumber" value={formData.mobileNumber} onChange={handleChange} />
              </div>
            </>
          ) : (
            <>
              <div className="profile-field"><strong>Facebook:</strong> {profile?.facebookUrl || 'N/A'}</div>
              <div className="profile-field"><strong>Messenger:</strong> {profile?.messenger || 'N/A'}</div>
              <div className="profile-field"><strong>Mobile:</strong> {profile?.mobileNumber || 'N/A'}</div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
