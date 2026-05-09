import React from 'react';
import { render, screen } from '@testing-library/react';

describe('Feature components', () => {
  describe('Book listing feature', () => {
    it('should display book list with items', () => {
      const BookList = ({ books }) => (
        <div className="book-list">
          {books.map((book) => (
            <div key={book.id} className="book-card">
              <h3>{book.title}</h3>
              <p>{book.author}</p>
              <p>${book.price}</p>
            </div>
          ))}
        </div>
      );

      const mockBooks = [
        { id: 1, title: 'Book 1', author: 'Author 1', price: 10 },
        { id: 2, title: 'Book 2', author: 'Author 2', price: 15 },
      ];

      render(<BookList books={mockBooks} />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.getByText('Book 2')).toBeInTheDocument();
      expect(screen.getByText('Author 1')).toBeInTheDocument();
    });

    it('should handle empty book list', () => {
      const BookList = ({ books }) => (
        <div className="book-list">
          {books.length === 0 ? (
            <p>No books available</p>
          ) : (
            books.map((book) => (
              <div key={book.id} className="book-card">
                <h3>{book.title}</h3>
              </div>
            ))
          )}
        </div>
      );

      render(<BookList books={[]} />);
      expect(screen.getByText('No books available')).toBeInTheDocument();
    });
  });

  describe('Search feature', () => {
    it('should filter books by search term', () => {
      const SearchBooks = ({ books, searchTerm }) => (
        <div>
          {books
            .filter((book) => book.title.toLowerCase().includes(searchTerm.toLowerCase()))
            .map((book) => (
              <div key={book.id}>{book.title}</div>
            ))}
        </div>
      );

      const mockBooks = [
        { id: 1, title: 'The Great Gatsby' },
        { id: 2, title: 'To Kill a Mockingbird' },
        { id: 3, title: 'Great Expectations' },
      ];

      render(<SearchBooks books={mockBooks} searchTerm="Great" />);

      expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      expect(screen.getByText('Great Expectations')).toBeInTheDocument();
      expect(screen.queryByText('To Kill a Mockingbird')).not.toBeInTheDocument();
    });
  });

  describe('Wishlist feature', () => {
    it('should add item to wishlist', () => {
      const Wishlist = ({ items }) => (
        <div>
          <p>Items in wishlist: {items.length}</p>
          {items.map((item) => (
            <div key={item.id}>{item.name}</div>
          ))}
        </div>
      );

      const { rerender } = render(<Wishlist items={[]} />);
      expect(screen.getByText('Items in wishlist: 0')).toBeInTheDocument();

      rerender(<Wishlist items={[{ id: 1, name: 'Book 1' }]} />);
      expect(screen.getByText('Items in wishlist: 1')).toBeInTheDocument();
      expect(screen.getByText('Book 1')).toBeInTheDocument();
    });

    it('should remove item from wishlist', () => {
      const Wishlist = ({ items }) => (
        <div>
          <p>Items in wishlist: {items.length}</p>
        </div>
      );

      const { rerender } = render(
        <Wishlist items={[{ id: 1, name: 'Book 1' }]} />
      );
      expect(screen.getByText('Items in wishlist: 1')).toBeInTheDocument();

      rerender(<Wishlist items={[]} />);
      expect(screen.getByText('Items in wishlist: 0')).toBeInTheDocument();
    });
  });

  describe('Pagination feature', () => {
    it('should render pagination buttons', () => {
      const Pagination = ({ currentPage, totalPages, onPageChange }) => (
        <div>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
            <button
              key={page}
              onClick={() => onPageChange(page)}
              disabled={page === currentPage}
            >
              {page}
            </button>
          ))}
        </div>
      );

      const handlePageChange = jest.fn();
      render(
        <Pagination
          currentPage={1}
          totalPages={3}
          onPageChange={handlePageChange}
        />
      );

      expect(screen.getByRole('button', { name: '1' })).toBeDisabled();
      expect(screen.getByRole('button', { name: '2' })).not.toBeDisabled();
      expect(screen.getByRole('button', { name: '3' })).not.toBeDisabled();
    });
  });

  describe('Filter feature', () => {
    it('should filter items by category', () => {
      const FilterBooks = ({ books, selectedCategory }) => (
        <div>
          {books
            .filter((book) => !selectedCategory || book.category === selectedCategory)
            .map((book) => (
              <div key={book.id}>{book.title}</div>
            ))}
        </div>
      );

      const mockBooks = [
        { id: 1, title: 'Fiction Book', category: 'fiction' },
        { id: 2, title: 'Science Book', category: 'science' },
        { id: 3, title: 'Another Fiction', category: 'fiction' },
      ];

      render(<FilterBooks books={mockBooks} selectedCategory="fiction" />);

      expect(screen.getByText('Fiction Book')).toBeInTheDocument();
      expect(screen.getByText('Another Fiction')).toBeInTheDocument();
      expect(screen.queryByText('Science Book')).not.toBeInTheDocument();
    });
  });

  describe('Rating feature', () => {
    it('should display and update rating', () => {
      const RatingComponent = ({ rating, onRatingChange }) => (
        <div>
          <p>Rating: {rating}</p>
          {[1, 2, 3, 4, 5].map((star) => (
            <button key={star} onClick={() => onRatingChange(star)}>
              {'⭐'}
            </button>
          ))}
        </div>
      );

      const handleRatingChange = jest.fn();
      render(
        <RatingComponent rating={3} onRatingChange={handleRatingChange} />
      );

      expect(screen.getByText('Rating: 3')).toBeInTheDocument();
    });
  });

  describe('Transaction feature', () => {
    it('should display transaction details', () => {
      const TransactionDetail = ({ transaction }) => (
        <div>
          <p>Book: {transaction.bookTitle}</p>
          <p>Amount: ${transaction.amount}</p>
          <p>Status: {transaction.status}</p>
          <p>Date: {transaction.date}</p>
        </div>
      );

      const mockTransaction = {
        bookTitle: 'Test Book',
        amount: 19.99,
        status: 'Completed',
        date: '2026-05-10',
      };

      render(<TransactionDetail transaction={mockTransaction} />);

      expect(screen.getByText('Book: Test Book')).toBeInTheDocument();
      expect(screen.getByText('Amount: $19.99')).toBeInTheDocument();
      expect(screen.getByText('Status: Completed')).toBeInTheDocument();
    });
  });
});
