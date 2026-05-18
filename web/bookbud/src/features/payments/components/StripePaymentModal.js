import React, { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import {
  Elements,
  CardElement,
  useStripe,
  useElements,
} from '@stripe/react-stripe-js';
import './StripePaymentModal.css';

// Replace with your Stripe publishable key
const stripePromise = loadStripe('pk_test_51TUqg6Ro1xWxMgIFkq351sppQGOHfpGHoCLEc5wMA7ETHJPrTYGt9sVLkz4M6KZ0XqUe8lDuDkY7npGLLdgCMDDK00yfg6YXk6');

const CheckoutForm = ({ clientSecret, onCancel, onSuccess, transactionId }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState(null);
  const [processing, setProcessing] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setProcessing(true);

    const result = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: elements.getElement(CardElement),
        billing_details: {
          name: 'BookBud User',
        },
      },
    });

    if (result.error) {
      setError(result.error.message);
      setProcessing(false);
    } else {
      if (result.paymentIntent.status === 'succeeded') {
        onSuccess();
      }
    }
  };

  const handleClear = () => {
    elements.getElement(CardElement).clear();
    setError(null);
  };

  return (
    <form onSubmit={handleSubmit} className="stripe-form">
      <div className="card-element-container">
        <label>Card Details</label>
        <CardElement
          options={{
            style: {
              base: {
                fontSize: '16px',
                color: '#424770',
                '::placeholder': {
                  color: '#aab7c4',
                },
              },
              invalid: {
                color: '#9e2146',
              },
            },
          }}
        />
      </div>
      
      {error && <div className="stripe-error">{error}</div>}
      
      <div className="stripe-actions">
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={processing}>
          Cancel
        </button>
        <button type="button" className="btn btn-outline" onClick={handleClear} disabled={processing}>
          Clear Card
        </button>
        <button type="submit" className="btn btn-primary" disabled={!stripe || processing}>
          {processing ? 'Processing...' : 'Pay Now'}
        </button>
      </div>
    </form>
  );
};

export default function StripePaymentModal({ clientSecret, transactionId, onCancel, onSuccess }) {
  if (!clientSecret) return null;

  return (
    <div className="stripe-modal-overlay">
      <div className="stripe-modal-content">
        <div className="stripe-modal-header">
          <h3>Secure Card Payment</h3>
          <p>Powered by Stripe</p>
        </div>
        <Elements stripe={stripePromise}>
          <CheckoutForm 
            clientSecret={clientSecret} 
            onCancel={onCancel} 
            onSuccess={onSuccess} 
            transactionId={transactionId}
          />
        </Elements>
      </div>
    </div>
  );
}
