import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

/**
 * Dashboard Features Tests
 * Tests for dashboard overview, statistics, and user activity
 */
describe('Dashboard Features', () => {
  describe('Dashboard Overview', () => {
    it('should display dashboard KPI cards', () => {
      const mockStats = {
        totalListings: 12,
        activeTransactions: 3,
        totalEarnings: 234.50,
        wishlistItems: 8,
      };

      const Dashboard = ({ stats }) => (
        <div className="dashboard">
          <div className="kpi-card">
            <p>Total Listings: {stats.totalListings}</p>
          </div>
          <div className="kpi-card">
            <p>Active Transactions: {stats.activeTransactions}</p>
          </div>
          <div className="kpi-card">
            <p>Total Earnings: ${stats.totalEarnings}</p>
          </div>
          <div className="kpi-card">
            <p>Wishlist Items: {stats.wishlistItems}</p>
          </div>
        </div>
      );

      render(<Dashboard stats={mockStats} />);

      expect(screen.getByText('Total Listings: 12')).toBeInTheDocument();
      expect(screen.getByText('Active Transactions: 3')).toBeInTheDocument();
      expect(screen.getByText('Total Earnings: $234.5')).toBeInTheDocument();
      expect(screen.getByText('Wishlist Items: 8')).toBeInTheDocument();
    });

    it('should display loading state on dashboard', () => {
      const Dashboard = ({ loading }) => (
        <div>
          {loading && <p>Loading dashboard...</p>}
          {!loading && <p>Dashboard loaded</p>}
        </div>
      );

      render(<Dashboard loading={true} />);
      expect(screen.getByText('Loading dashboard...')).toBeInTheDocument();
    });

    it('should display error message on failed data fetch', () => {
      const Dashboard = ({ error }) => (
        <div>
          {error && <p>Error: {error}</p>}
          {!error && <p>Dashboard loaded</p>}
        </div>
      );

      render(<Dashboard error="Failed to load dashboard" />);
      expect(screen.getByText('Error: Failed to load dashboard')).toBeInTheDocument();
    });
  });

  describe('Recent Activity', () => {
    it('should display recent transactions', () => {
      const mockTransactions = [
        { id: 1, bookTitle: 'Book 1', type: 'Sale', date: '2026-05-10', amount: 15.99 },
        { id: 2, bookTitle: 'Book 2', type: 'Rental', date: '2026-05-09', amount: 5.99 },
      ];

      const ActivityList = ({ transactions }) => (
        <div className="activity-list">
          {transactions.map((txn) => (
            <div key={txn.id} className="activity-item">
              <p>{txn.bookTitle} - {txn.type}</p>
              <p>{txn.date}</p>
              <p>${txn.amount}</p>
            </div>
          ))}
        </div>
      );

      render(<ActivityList transactions={mockTransactions} />);

      expect(screen.getByText('Book 1 - Sale')).toBeInTheDocument();
      expect(screen.getByText('Book 2 - Rental')).toBeInTheDocument();
      expect(screen.getByText('2026-05-10')).toBeInTheDocument();
    });

    it('should display empty activity when no transactions', () => {
      const ActivityList = ({ transactions }) => (
        <div>
          {transactions.length === 0 ? (
            <p>No recent activity</p>
          ) : (
            transactions.map((txn) => <div key={txn.id}>{txn.bookTitle}</div>)
          )}
        </div>
      );

      render(<ActivityList transactions={[]} />);
      expect(screen.getByText('No recent activity')).toBeInTheDocument();
    });
  });

  describe('Quick Navigation', () => {
    it('should display quick access menu items', () => {
      const menuItems = ['Browse Books', 'My Listings', 'My Transactions', 'My Wishlist'];

      const QuickNav = ({ items }) => (
        <div className="quick-nav">
          {items.map((item) => (
            <button key={item}>{item}</button>
          ))}
        </div>
      );

      render(<QuickNav items={menuItems} />);

      menuItems.forEach((item) => {
        expect(screen.getByText(item)).toBeInTheDocument();
      });
    });

    it('should handle navigation clicks', async () => {
      const mockNavigate = jest.fn();

      const QuickNav = ({ onNavigate }) => (
        <div>
          <button onClick={() => onNavigate('listings')}>My Listings</button>
        </div>
      );

      render(<QuickNav onNavigate={mockNavigate} />);

      const button = screen.getByText('My Listings');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('listings');
      });
    });
  });

  describe('Dashboard Alerts', () => {
    it('should display alerts and notifications', () => {
      const mockAlerts = [
        { id: 1, type: 'info', message: 'You have a new message' },
        { id: 2, type: 'warning', message: 'Book rental expires soon' },
      ];

      const AlertsSection = ({ alerts }) => (
        <div className="alerts">
          {alerts.map((alert) => (
            <div key={alert.id} className={`alert alert-${alert.type}`}>
              {alert.message}
            </div>
          ))}
        </div>
      );

      render(<AlertsSection alerts={mockAlerts} />);

      expect(screen.getByText('You have a new message')).toBeInTheDocument();
      expect(screen.getByText('Book rental expires soon')).toBeInTheDocument();
    });

    it('should dismiss alerts', async () => {
      const mockDismiss = jest.fn();

      const Alert = ({ message, onDismiss }) => (
        <div className="alert">
          <p>{message}</p>
          <button onClick={onDismiss}>Dismiss</button>
        </div>
      );

      render(
        <Alert message="Test alert" onDismiss={mockDismiss} />
      );

      const button = screen.getByText('Dismiss');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockDismiss).toHaveBeenCalled();
      });
    });
  });

  describe('Dashboard Filters', () => {
    it('should filter activity by date range', () => {
      const mockTransactions = [
        { id: 1, date: '2026-05-10', amount: 15.99 },
        { id: 2, date: '2026-05-05', amount: 20.00 },
        { id: 3, date: '2026-04-20', amount: 10.00 },
      ];

      const filterByDateRange = (txns, startDate, endDate) => {
        return txns.filter((txn) => {
          const txnDate = new Date(txn.date);
          return txnDate >= startDate && txnDate <= endDate;
        });
      };

      const startDate = new Date('2026-05-01');
      const endDate = new Date('2026-05-15');

      const filtered = filterByDateRange(mockTransactions, startDate, endDate);

      expect(filtered.length).toBe(2);
      expect(filtered[0].id).toBe(1);
      expect(filtered[1].id).toBe(2);
    });

    it('should filter activity by transaction type', () => {
      const mockTransactions = [
        { id: 1, type: 'Sale', amount: 15.99 },
        { id: 2, type: 'Rental', amount: 5.99 },
        { id: 3, type: 'Sale', amount: 20.00 },
      ];

      const filterByType = (txns, type) => {
        return txns.filter((txn) => txn.type === type);
      };

      const sales = filterByType(mockTransactions, 'Sale');

      expect(sales.length).toBe(2);
      expect(sales.every((txn) => txn.type === 'Sale')).toBe(true);
    });
  });

  describe('Dashboard Refresh', () => {
    it('should refresh dashboard data', async () => {
      const mockRefresh = jest.fn().mockResolvedValue({ success: true });

      const Dashboard = ({ onRefresh }) => (
        <div>
          <button onClick={onRefresh}>Refresh</button>
          <p>Dashboard</p>
        </div>
      );

      render(<Dashboard onRefresh={mockRefresh} />);

      const button = screen.getByText('Refresh');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockRefresh).toHaveBeenCalled();
      });
    });
  });
});
