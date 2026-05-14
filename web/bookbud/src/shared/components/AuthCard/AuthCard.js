import React from 'react';
import PropTypes from 'prop-types';
import styles from './AuthCard.module.css';

const AuthCard = ({ children, className }) => {
  return (
    <div className={`${styles.card} ${className || ''}`}>
      {children}
    </div>
  );
};

AuthCard.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

AuthCard.defaultProps = {
  className: '',
};

export default AuthCard;
