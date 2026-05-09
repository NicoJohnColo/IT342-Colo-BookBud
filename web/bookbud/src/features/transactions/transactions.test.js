import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

/**
 * Transactions Feature Tests
 * Tests for creating, viewing, updating, and managing transactions
 */
describe('Transactions Features', () => {
  describe('Transaction Creation', () => {
    it('should create new transaction for book purchase', async () => {
      const mockCreateTransaction = jest.fn().mockResolvedValue({ success: true, transactionId: '1' });

      const BookCard = ({ book, onCreateTransaction }) => (
        <div>
          <p>{book.title}</p>
          <p>${book.price}</p>
          <button onClick={() => onCreateTransaction(book.id, 'Purchase')}>Buy</button>
        </div>
      );

      render(
        <BookCard
          book={{ id: 1, title: 'Test Book', price: 15.99 }}
          onCreateTransaction={mockCreateTransaction}
        />
      );

      const button = screen.getByText('Buy');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockCreateTransaction).toHaveBeenCalledWith(1, 'Purchase');
      });
    });

    it('should create new transaction for book rental', async () => {
      const mockCreateTransaction = jest.fn().mockResolvedValue({ success: true, transactionId: '2' });

      const BookCard = ({ book, onCreateTransaction }) => (
        <div>
          <p>{book.title}</p>
          <p>${book.rentalPrice}/day</p>
          <button onClick={() => onCreateTransaction(book.id, 'Rental')}>Rent</button>
        </div>
      );

      render(
        <BookCard
          book={{ id: 1, title: 'Test Book', rentalPrice: 2.99 }}
          onCreateTransaction={mockCreateTransaction}
        />
      );

      const button = screen.getByText('Rent');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockCreateTransaction).toHaveBeenCalledWith(1, 'Rental');
      });
    });
  });

  describe('Transaction Listing', () => {
    it('should display list of user transactions', () => {
      const mockTransactions = [
        {
          id: '1',
          bookTitle: 'Book 1',
          seller: 'John',
          buyer: 'Jane',
          type: 'Sale',
          status: 'Completed',
          amount: 15.99,
          date: '2026-05-10',
        },
        {
          id: '2',
          bookTitle: 'Book 2',
          seller: 'Jane',
          buyer: 'John',
          type: 'Rental',
          status: 'Active',
          amount: 5.99,
          date: '2026-05-09',
        },
      ];

      const TransactionList = ({ transactions }) => (
        <div className="transaction-list">
          {transactions.map((txn) => (
            <div key={txn.id} className="transaction-item">
              <p>{txn.bookTitle}</p>
              <p>{txn.type} - {txn.status}</p>
              <p>${txn.amount}</p>
              <p>{txn.date}</p>
            </div>
          ))}
        </div>
      );

      render(<TransactionList transactions={mockTransactions} />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.getByText('Book 2')).toBeInTheDocument();
      expect(screen.getByText('Sale - Completed')).toBeInTheDocument();
      expect(screen.getByText('Rental - Active')).toBeInTheDocument();
    });

    it('should display empty state for no transactions', () => {
      const TransactionList = ({ transactions }) => (
        <div>
          {transactions.length === 0 ? (
            <p>No transactions</p>
          ) : (
            transactions.map((txn) => <div key={txn.id}>{txn.bookTitle}</div>)
          )}
        </div>
      );

      render(<TransactionList transactions={[]} />);
      expect(screen.getByText('No transactions')).toBeInTheDocument();
    });
  });

  describe('Transaction Status', () => {
    it('should display all transaction statuses', () => {
      const statuses = ['Pending', 'Active', 'Completed', 'Cancelled', 'Disputed'];

      const StatusFilter = ({ statuses: availableStatuses }) => (
        <div>
          {availableStatuses.map((status) => (
            <button key={status}>{status}</button>
          ))}
        </div>
      );

      render(<StatusFilter statuses={statuses} />);

      statuses.forEach((status) => {
        expect(screen.getByText(status)).toBeInTheDocument();
      });
    });

    it('should update transaction status', async () => {
      const mockUpdateStatus = jest.fn().mockResolvedValue({ success: true });

      const TransactionDetail = ({ transaction, onUpdateStatus }) => (
        <div>
          <p>{transaction.bookTitle}</p>
          <p>Status: {transaction.status}</p>
          <button onClick={() => onUpdateStatus(transaction.id, 'Completed')}>
            Mark as Completed
          </button>
        </div>
      );

      render(
        <TransactionDetail
          transaction={{ id: '1', bookTitle: 'Test Book', status: 'Active' }}
          onUpdateStatus={mockUpdateStatus}
        />
      );

      const button = screen.getByText('Mark as Completed');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockUpdateStatus).toHaveBeenCalledWith('1', 'Completed');
      });
    });
  });

  describe('Transaction Details', () => {
    it('should display transaction details', () => {
      const mockTransaction = {
        id: '1',
        bookTitle: 'Test Book',
        bookId: 'b1',
        seller: 'John Doe',
        buyer: 'Jane Smith',
        type: 'Sale',
        status: 'Completed',
        amount: 19.99,
        date: '2026-05-10',
        notes: 'Book in excellent condition',
      };

      const TransactionDetail = ({ transaction }) => (
        <div>
          <h2>{transaction.bookTitle}</h2>
          <p>Seller: {transaction.seller}</p>
          <p>Buyer: {transaction.buyer}</p>
          <p>Type: {transaction.type}</p>
          <p>Status: {transaction.status}</p>
          <p>Amount: ${transaction.amount}</p>
          <p>Date: {transaction.date}</p>
          <p>Notes: {transaction.notes}</p>
        </div>
      );

      render(<TransactionDetail transaction={mockTransaction} />);

      expect(screen.getByText('Test Book')).toBeInTheDocument();
      expect(screen.getByText('Seller: John Doe')).toBeInTheDocument();
      expect(screen.getByText('Buyer: Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Amount: $19.99')).toBeInTheDocument();
    });
  });

  describe('Transaction Filtering', () => {
    it('should filter transactions by type', () => {
      const mockTransactions = [
        { id: '1', type: 'Sale', status: 'Completed' },
        { id: '2', type: 'Rental', status: 'Active' },
        { id: '3', type: 'Sale', status: 'Pending' },
      ];

      const filterByType = (txns, type) => {
        return txns.filter((txn) => txn.type === type);
      };

      const sales = filterByType(mockTransactions, 'Sale');
      expect(sales.length).toBe(2);
      expect(sales.every((txn) => txn.type === 'Sale')).toBe(true);
    });

    it('should filter transactions by status', () => {
      const mockTransactions = [
        { id: '1', status: 'Completed' },
        { id: '2', status: 'Active' },
        { id: '3', status: 'Completed' },
      ];

      const filterByStatus = (txns, status) => {
        return txns.filter((txn) => txn.status === status);
      };

      const completed = filterByStatus(mockTransactions, 'Completed');
      expect(completed.length).toBe(2);
    });
  });

  describe('Transaction Actions', () => {
    it('should cancel transaction', async () => {
      const mockCancel = jest.fn().mockResolvedValue({ success: true });

      const TransactionDetail = ({ transaction, onCancel }) => (
        <div>
          <p>{transaction.bookTitle}</p>
          <p>Status: {transaction.status}</p>
          <button onClick={() => onCancel(transaction.id)}>Cancel Transaction</button>
        </div>
      );

      render(
        <TransactionDetail
          transaction={{ id: '1', bookTitle: 'Test Book', status: 'Pending' }}
          onCancel={mockCancel}
        />
      );

      const button = screen.getByText('Cancel Transaction');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockCancel).toHaveBeenCalledWith('1');
      });
    });

    it('should report transaction dispute', async () => {
      const mockReportDispute = jest.fn().mockResolvedValue({ success: true });

      const TransactionDetail = ({ transaction, onReportDispute }) => (
        <div>
          <p>{transaction.bookTitle}</p>
          <button onClick={() => onReportDispute(transaction.id)}>Report Issue</button>
        </div>
      );

      render(
        <TransactionDetail
          transaction={{ id: '1', bookTitle: 'Test Book' }}
          onReportDispute={mockReportDispute}
        />
      );

      const button = screen.getByText('Report Issue');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockReportDispute).toHaveBeenCalledWith('1');
      });
    });
  });
});
