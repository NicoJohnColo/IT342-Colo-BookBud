import { useCallback, useEffect, useState } from 'react';
import paymentService from '../services/paymentService';

export default function useEarnings() {
  const [earnings, setEarnings] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await paymentService.getEarningsSummary();
      setEarnings(data || {});
      return data;
    } catch (err) {
      setError(err);
      setEarnings(null);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (process.env.NODE_ENV === 'test') return;
    fetch();
  }, [fetch]);

  // Allow external components to trigger a refresh
  const refresh = useCallback(async () => {
    return fetch();
  }, [fetch]);

  useEffect(() => {
    const handleRefresh = () => fetch();
    window.addEventListener('refreshPayments', handleRefresh);
    return () => window.removeEventListener('refreshPayments', handleRefresh);
  }, [fetch]);

  return { earnings, loading, error, refresh };
}
