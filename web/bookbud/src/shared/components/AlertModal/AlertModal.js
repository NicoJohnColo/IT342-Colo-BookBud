import React from 'react';
import './AlertModal.css';

const AlertModal = ({ isOpen, onClose, title, message, type = 'info' }) => {
  if (!isOpen) return null;

  const getTypeClass = () => {
    switch (type) {
      case 'error': return 'alert-modal-error';
      case 'warning': return 'alert-modal-warning';
      case 'success': return 'alert-modal-success';
      default: return 'alert-modal-info';
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'error': return '❌';
      case 'warning': return '⚠️';
      case 'success': return '✅';
      default: return 'ℹ️';
    }
  };

  return (
    <div className="alert-modal-overlay" onClick={onClose}>
      <div className="alert-modal" onClick={(e) => e.stopPropagation()}>
        <div className={`alert-modal-header ${getTypeClass()}`}>
          <span className="alert-modal-icon">{getIcon()}</span>
          <h3>{title}</h3>
          <button className="alert-modal-close" onClick={onClose}>×</button>
        </div>
        <div className="alert-modal-body">
          <p>{message}</p>
        </div>
        <div className="alert-modal-footer">
          <button className="btn btn-primary" onClick={onClose}>
            OK
          </button>
        </div>
      </div>
    </div>
  );
};

export default AlertModal;
