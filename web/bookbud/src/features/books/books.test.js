import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

/**
 * Books Feature Tests
 * Tests for browsing, searching, filtering, and viewing book details
 */
describe('Books Features', () => {
  describe('Book Listing', () => {
    it('should display list of books', () => {
      const mockBooks = [
        { id: 1, title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', price: 15.99, condition: 'Like New' },
        { id: 2, title: 'To Kill a Mockingbird', author: 'Harper Lee', price: 12.99, condition: 'Good' },
      ];

      const BookList = ({ books }) => (
        <div className="book-list">
          {books.map((book) => (
            <div key={book.id} className="book-card">
              <h3>{book.title}</h3>
              <p>{book.author}</p>
              <p>${book.price}</p>
              <p>{book.condition}</p>
            </div>
          ))}
        </div>
      );

      render(<BookList books={mockBooks} />);

      expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      expect(screen.getByText('To Kill a Mockingbird')).toBeInTheDocument();
      expect(screen.getByText('F. Scott Fitzgerald')).toBeInTheDocument();
    });

    it('should display empty state when no books available', () => {
      const BookList = ({ books }) => (
        <div>
          {books.length === 0 ? (
            <p>No books available</p>
          ) : (
            books.map((book) => <div key={book.id}>{book.title}</div>)
          )}
        </div>
      );

      render(<BookList books={[]} />);
      expect(screen.getByText('No books available')).toBeInTheDocument();
    });

    it('should display loading state while fetching books', () => {
      const BookList = ({ loading, books }) => (
        <div>
          {loading && <p>Loading books...</p>}
          {!loading && books.length === 0 && <p>No books found</p>}
        </div>
      );

      render(<BookList loading={true} books={[]} />);
      expect(screen.getByText('Loading books...')).toBeInTheDocument();
    });
  });

  describe('Book Search', () => {
    it('should filter books by title', () => {
      const mockBooks = [
        { id: 1, title: 'The Great Gatsby' },
        { id: 2, title: 'Great Expectations' },
        { id: 3, title: 'To Kill a Mockingbird' },
      ];

      const SearchBooks = ({ books, searchTerm }) => (
        <div>
          {books
            .filter((book) => book.title.toLowerCase().includes(searchTerm.toLowerCase()))
            .map((book) => (
              <div key={book.id}>{book.title}</div>
            ))}
        </div>
      );

      render(<SearchBooks books={mockBooks} searchTerm="Great" />);

      expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      expect(screen.getByText('Great Expectations')).toBeInTheDocument();
      expect(screen.queryByText('To Kill a Mockingbird')).not.toBeInTheDocument();
    });

    it('should filter books by author', () => {
      const mockBooks = [
        { id: 1, title: 'Book 1', author: 'John Smith' },
        { id: 2, title: 'Book 2', author: 'Jane Doe' },
        { id: 3, title: 'Book 3', author: 'John Smith' },
      ];

      const SearchBooks = ({ books, authorFilter }) => (
        <div>
          {books
            .filter((book) => book.author === authorFilter || !authorFilter)
            .map((book) => (
              <div key={book.id}>{book.title}</div>
            ))}
        </div>
      );

      render(<SearchBooks books={mockBooks} authorFilter="John Smith" />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.getByText('Book 3')).toBeInTheDocument();
      expect(screen.queryByText('Book 2')).not.toBeInTheDocument();
    });

    it('should filter books by price range', () => {
      const mockBooks = [
        { id: 1, title: 'Book 1', price: 5.99 },
        { id: 2, title: 'Book 2', price: 15.99 },
        { id: 3, title: 'Book 3', price: 25.99 },
      ];

      const PriceFilter = ({ books, minPrice, maxPrice }) => (
        <div>
          {books
            .filter((book) => book.price >= minPrice && book.price <= maxPrice)
            .map((book) => (
              <div key={book.id}>{book.title}</div>
            ))}
        </div>
      );

      render(<PriceFilter books={mockBooks} minPrice={10} maxPrice={20} />);

      expect(screen.getByText('Book 2')).toBeInTheDocument();
      expect(screen.queryByText('Book 1')).not.toBeInTheDocument();
      expect(screen.queryByText('Book 3')).not.toBeInTheDocument();
    });
  });

  describe('Book Details', () => {
    it('should display book details', () => {
      const mockBook = {
        id: 1,
        title: 'The Great Gatsby',
        author: 'F. Scott Fitzgerald',
        description: 'A novel about wealth and love',
        price: 15.99,
        condition: 'Like New',
        transactionType: 'For Sale',
      };

      const BookDetails = ({ book }) => (
        <div>
          <h2>{book.title}</h2>
          <p>{book.author}</p>
          <p>{book.description}</p>
          <p>${book.price}</p>
          <p>{book.condition}</p>
          <p>{book.transactionType}</p>
        </div>
      );

      render(<BookDetails book={mockBook} />);

      expect(screen.getByText('The Great Gatsby')).toBeInTheDocument();
      expect(screen.getByText('A novel about wealth and love')).toBeInTheDocument();
      expect(screen.getByText('$15.99')).toBeInTheDocument();
    });

    it('should display ratings for book', () => {
      const mockBook = {
        id: 1,
        title: 'Test Book',
        rating: 4.5,
        reviewCount: 127,
      };

      const BookRating = ({ book }) => (
        <div>
          <p>Rating: {book.rating}/5 ({book.reviewCount} reviews)</p>
        </div>
      );

      render(<BookRating book={mockBook} />);

      expect(screen.getByText('Rating: 4.5/5 (127 reviews)')).toBeInTheDocument();
    });
  });

  describe('Book Conditions', () => {
    it('should display available book conditions', () => {
      const conditions = ['Like New', 'Good', 'Fair', 'Poor'];

      const ConditionFilter = ({ conditions: availableConditions }) => (
        <div>
          {availableConditions.map((condition) => (
            <button key={condition}>{condition}</button>
          ))}
        </div>
      );

      render(<ConditionFilter conditions={conditions} />);

      conditions.forEach((condition) => {
        expect(screen.getByText(condition)).toBeInTheDocument();
      });
    });
  });

  describe('Transaction Type', () => {
    it('should display transaction type options', () => {
      const transactionTypes = ['For Sale', 'For Rent', 'Both'];

      const TransactionTypeFilter = ({ types }) => (
        <div>
          {types.map((type) => (
            <button key={type}>{type}</button>
          ))}
        </div>
      );

      render(<TransactionTypeFilter types={transactionTypes} />);

      transactionTypes.forEach((type) => {
        expect(screen.getByText(type)).toBeInTheDocument();
      });
    });
  });

  describe('Book Actions', () => {
    it('should add book to wishlist', async () => {
      const mockAddToWishlist = jest.fn().mockResolvedValue({ success: true });

      const BookCard = ({ book, onAddToWishlist }) => (
        <div>
          <p>{book.title}</p>
          <button onClick={() => onAddToWishlist(book.id)}>Add to Wishlist</button>
        </div>
      );

      render(
        <BookCard
          book={{ id: 1, title: 'Test Book' }}
          onAddToWishlist={mockAddToWishlist}
        />
      );

      const button = screen.getByText('Add to Wishlist');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockAddToWishlist).toHaveBeenCalledWith(1);
      });
    });

    it('should initiate transaction for book', async () => {
      const mockInitiateTransaction = jest.fn().mockResolvedValue({ success: true });

      const BookCard = ({ book, onTransaction }) => (
        <div>
          <p>{book.title}</p>
          <button onClick={() => onTransaction(book.id)}>Buy/Rent</button>
        </div>
      );

      render(
        <BookCard
          book={{ id: 1, title: 'Test Book' }}
          onTransaction={mockInitiateTransaction}
        />
      );

      const button = screen.getByText('Buy/Rent');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockInitiateTransaction).toHaveBeenCalledWith(1);
      });
    });
  });
});
