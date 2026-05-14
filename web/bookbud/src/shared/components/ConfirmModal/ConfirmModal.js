import React from 'react';
import './ConfirmModal.css';

const ConfirmModal = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  type = 'danger',
  disabled = false
}) => {
  if (!isOpen) return null;

  const getTypeClass = () => {
    switch (type) {
      case 'danger': return 'confirm-modal-danger';
      case 'warning': return 'confirm-modal-warning';
      case 'info': return 'confirm-modal-info';
      default: return 'confirm-modal-primary';
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'danger': return '⚠️';
      case 'warning': return '⚠️';
      case 'info': return 'ℹ️';
      default: return '❓';
    }
  };

  return (
    <div className="confirm-modal-overlay" onClick={onClose}>
      <div className="confirm-modal" onClick={(e) => e.stopPropagation()}>
        <div className={`confirm-modal-header ${getTypeClass()}`}>
          <span className="confirm-modal-icon">{getIcon()}</span>
          <h3>{title}</h3>
          <button className="confirm-modal-close" onClick={onClose}>×</button>
        </div>
        <div className="confirm-modal-body">
          <p>{message}</p>
        </div>
        <div className="confirm-modal-footer">
          <button 
            className="btn btn-secondary" 
            onClick={onClose}
            disabled={disabled}
          >
            {cancelText}
          </button>
          <button 
            className={`btn ${type === 'danger' ? 'btn-danger' : 'btn-primary'}`} 
            onClick={onConfirm}
            disabled={disabled}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
