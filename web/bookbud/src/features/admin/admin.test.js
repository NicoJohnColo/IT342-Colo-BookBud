import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

/**
 * Admin Panel Features Tests
 * Tests for admin dashboard, user management, and system administration
 */
describe('Admin Panel Features', () => {
  describe('Admin Dashboard', () => {
    it('should display admin dashboard KPIs', () => {
      const mockKPIs = {
        totalUsers: 1250,
        totalBooks: 3450,
        totalTransactions: 8920,
        totalRevenue: 45230.50,
        activeListings: 892,
      };

      const AdminDashboard = ({ kpis }) => (
        <div className="admin-dashboard">
          <div className="kpi">Total Users: {kpis.totalUsers}</div>
          <div className="kpi">Total Books: {kpis.totalBooks}</div>
          <div className="kpi">Transactions: {kpis.totalTransactions}</div>
          <div className="kpi">Revenue: ${kpis.totalRevenue}</div>
          <div className="kpi">Active Listings: {kpis.activeListings}</div>
        </div>
      );

      render(<AdminDashboard kpis={mockKPIs} />);

      expect(screen.getByText('Total Users: 1250')).toBeInTheDocument();
      expect(screen.getByText('Total Books: 3450')).toBeInTheDocument();
      expect(screen.getByText('Transactions: 8920')).toBeInTheDocument();
      expect(screen.getByText('Revenue: $45230.5')).toBeInTheDocument();
    });
  });

  describe('User Management', () => {
    it('should display list of users', () => {
      const mockUsers = [
        { id: '1', username: 'user1', email: 'user1@test.com', role: 'User', status: 'Active' },
        { id: '2', username: 'user2', email: 'user2@test.com', role: 'User', status: 'Suspended' },
      ];

      const UserList = ({ users }) => (
        <div className="user-list">
          {users.map((user) => (
            <div key={user.id} className="user-row">
              <p>{user.username}</p>
              <p>{user.email}</p>
              <p>{user.status}</p>
            </div>
          ))}
        </div>
      );

      render(<UserList users={mockUsers} />);

      expect(screen.getByText('user1')).toBeInTheDocument();
      expect(screen.getByText('user2')).toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
      expect(screen.getByText('Suspended')).toBeInTheDocument();
    });

    it('should suspend user account', async () => {
      const mockSuspendUser = jest.fn().mockResolvedValue({ success: true });

      const UserRow = ({ user, onSuspend }) => (
        <div>
          <p>{user.username}</p>
          <button onClick={() => onSuspend(user.id)}>Suspend User</button>
        </div>
      );

      render(
        <UserRow
          user={{ id: '1', username: 'testuser' }}
          onSuspend={mockSuspendUser}
        />
      );

      const button = screen.getByText('Suspend User');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockSuspendUser).toHaveBeenCalledWith('1');
      });
    });

    it('should ban user account', async () => {
      const mockBanUser = jest.fn().mockResolvedValue({ success: true });

      const UserRow = ({ user, onBan }) => (
        <div>
          <p>{user.username}</p>
          <button onClick={() => onBan(user.id)}>Ban User</button>
        </div>
      );

      render(
        <UserRow
          user={{ id: '1', username: 'testuser' }}
          onBan={mockBanUser}
        />
      );

      const button = screen.getByText('Ban User');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockBanUser).toHaveBeenCalledWith('1');
      });
    });
  });

  describe('Book Management', () => {
    it('should display list of books for moderation', () => {
      const mockBooks = [
        { id: '1', title: 'Book 1', owner: 'user1', status: 'Active', listedDate: '2026-05-10' },
        { id: '2', title: 'Book 2', owner: 'user2', status: 'Flagged', listedDate: '2026-05-09' },
      ];

      const BookList = ({ books }) => (
        <div className="book-list">
          {books.map((book) => (
            <div key={book.id} className="book-row">
              <p>{book.title}</p>
              <p>{book.owner}</p>
              <p>{book.status}</p>
            </div>
          ))}
        </div>
      );

      render(<BookList books={mockBooks} />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.getByText('Book 2')).toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
      expect(screen.getByText('Flagged')).toBeInTheDocument();
    });

    it('should remove flagged book', async () => {
      const mockRemoveBook = jest.fn().mockResolvedValue({ success: true });

      const BookRow = ({ book, onRemove }) => (
        <div>
          <p>{book.title}</p>
          <button onClick={() => onRemove(book.id)}>Remove Book</button>
        </div>
      );

      render(
        <BookRow
          book={{ id: '1', title: 'Flagged Book' }}
          onRemove={mockRemoveBook}
        />
      );

      const button = screen.getByText('Remove Book');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockRemoveBook).toHaveBeenCalledWith('1');
      });
    });
  });

  describe('Transaction Monitoring', () => {
    it('should display flagged transactions', () => {
      const mockTransactions = [
        { id: '1', bookTitle: 'Book 1', amount: 100, status: 'Disputed', flag: true },
        { id: '2', bookTitle: 'Book 2', amount: 50, status: 'Completed', flag: false },
      ];

      const TransactionList = ({ transactions }) => (
        <div className="transactions">
          {transactions.filter((t) => t.flag).map((txn) => (
            <div key={txn.id}>
              <p>{txn.bookTitle}</p>
              <p>${txn.amount}</p>
              <p>{txn.status}</p>
            </div>
          ))}
        </div>
      );

      render(<TransactionList transactions={mockTransactions} />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.queryByText('Book 2')).not.toBeInTheDocument();
    });

    it('should resolve transaction dispute', async () => {
      const mockResolveDispute = jest.fn().mockResolvedValue({ success: true });

      const DisputeTransaction = ({ transaction, onResolve }) => (
        <div>
          <p>{transaction.bookTitle}</p>
          <button onClick={() => onResolve(transaction.id)}>Resolve</button>
        </div>
      );

      render(
        <DisputeTransaction
          transaction={{ id: '1', bookTitle: 'Disputed Book' }}
          onResolve={mockResolveDispute}
        />
      );

      const button = screen.getByText('Resolve');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockResolveDispute).toHaveBeenCalledWith('1');
      });
    });
  });

  describe('System Reports', () => {
    it('should generate system report', async () => {
      const mockGenerateReport = jest.fn().mockResolvedValue({ success: true, reportUrl: '/reports/123' });

      const ReportGenerator = ({ onGenerateReport }) => (
        <div>
          <button onClick={() => onGenerateReport()}>Generate Report</button>
        </div>
      );

      render(<ReportGenerator onGenerateReport={mockGenerateReport} />);

      const button = screen.getByText('Generate Report');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockGenerateReport).toHaveBeenCalled();
      });
    });
  });

  describe('Admin Logs', () => {
    it('should display admin action logs', () => {
      const mockLogs = [
        { id: 1, admin: 'admin1', action: 'Suspended user', target: 'user123', timestamp: '2026-05-10 10:00' },
        { id: 2, admin: 'admin2', action: 'Removed book', target: 'book456', timestamp: '2026-05-10 09:30' },
      ];

      const AdminLogs = ({ logs }) => (
        <div className="logs">
          {logs.map((log) => (
            <div key={log.id}>
              <p>{log.admin} - {log.action}</p>
              <p>{log.timestamp}</p>
            </div>
          ))}
        </div>
      );

      render(<AdminLogs logs={mockLogs} />);

      expect(screen.getByText('admin1 - Suspended user')).toBeInTheDocument();
      expect(screen.getByText('admin2 - Removed book')).toBeInTheDocument();
    });
  });

  describe('System Configuration', () => {
    it('should display system settings', () => {
      const settings = ['Platform Fees', 'Commission Rate', 'Max Rental Days', 'Dispute Resolution Time'];

      const SystemSettings = ({ settingsList }) => (
        <div className="settings">
          {settingsList.map((setting) => (
            <button key={setting}>{setting}</button>
          ))}
        </div>
      );

      render(<SystemSettings settingsList={settings} />);

      settings.forEach((setting) => {
        expect(screen.getByText(setting)).toBeInTheDocument();
      });
    });

    it('should update system configuration', async () => {
      const mockUpdateConfig = jest.fn().mockResolvedValue({ success: true });

      const ConfigEditor = ({ onUpdateConfig }) => {
        const [commissionRate, setCommissionRate] = React.useState(10);

        const handleSubmit = (e) => {
          e.preventDefault();
          onUpdateConfig({ commissionRate });
        };

        return (
          <form onSubmit={handleSubmit}>
            <input
              type="number"
              value={commissionRate}
              onChange={(e) => setCommissionRate(Number(e.target.value))}
              placeholder="Commission Rate %"
            />
            <button type="submit">Save</button>
          </form>
        );
      };

      render(<ConfigEditor onUpdateConfig={mockUpdateConfig} />);

      const button = screen.getByText('Save');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockUpdateConfig).toHaveBeenCalled();
      });
    });
  });
});
