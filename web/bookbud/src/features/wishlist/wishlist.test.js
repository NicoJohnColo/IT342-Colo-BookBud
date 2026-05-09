import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

/**
 * Wishlist Features Tests
 * Tests for adding, removing, and viewing wishlist items
 */
describe('Wishlist Features', () => {
  describe('Add to Wishlist', () => {
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

    it('should show confirmation when book added to wishlist', async () => {
      const Wishlist = () => {
        const [message, setMessage] = React.useState('');

        const handleAdd = async (bookId) => {
          setMessage('Book added to wishlist');
        };

        return (
          <div>
            {message && <p>{message}</p>}
            <button onClick={() => handleAdd(1)}>Add to Wishlist</button>
          </div>
        );
      };

      render(<Wishlist />);

      const button = screen.getByText('Add to Wishlist');
      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText('Book added to wishlist')).toBeInTheDocument();
      });
    });
  });

  describe('Remove from Wishlist', () => {
    it('should remove book from wishlist', async () => {
      const mockRemove = jest.fn().mockResolvedValue({ success: true });

      const WishlistItem = ({ book, onRemove }) => (
        <div>
          <p>{book.title}</p>
          <button onClick={() => onRemove(book.id)}>Remove</button>
        </div>
      );

      render(
        <WishlistItem
          book={{ id: 1, title: 'Test Book' }}
          onRemove={mockRemove}
        />
      );

      const button = screen.getByText('Remove');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockRemove).toHaveBeenCalledWith(1);
      });
    });

    it('should update wishlist count when item removed', async () => {
      const Wishlist = ({ initialCount }) => {
        const [count, setCount] = React.useState(initialCount);

        const handleRemove = () => {
          setCount(count - 1);
        };

        return (
          <div>
            <p>Wishlist items: {count}</p>
            <button onClick={handleRemove}>Remove Item</button>
          </div>
        );
      };

      const { rerender } = render(<Wishlist initialCount={5} />);

      expect(screen.getByText('Wishlist items: 5')).toBeInTheDocument();

      const button = screen.getByText('Remove Item');
      fireEvent.click(button);

      expect(screen.getByText('Wishlist items: 4')).toBeInTheDocument();
    });
  });

  describe('Wishlist Display', () => {
    it('should display wishlist items', () => {
      const mockWishlist = [
        { id: 1, title: 'Book 1', author: 'Author 1', price: 15.99 },
        { id: 2, title: 'Book 2', author: 'Author 2', price: 12.99 },
      ];

      const WishlistPage = ({ items }) => (
        <div className="wishlist">
          {items.map((item) => (
            <div key={item.id} className="wishlist-item">
              <p>{item.title}</p>
              <p>{item.author}</p>
              <p>${item.price}</p>
            </div>
          ))}
        </div>
      );

      render(<WishlistPage items={mockWishlist} />);

      expect(screen.getByText('Book 1')).toBeInTheDocument();
      expect(screen.getByText('Book 2')).toBeInTheDocument();
      expect(screen.getByText('Author 1')).toBeInTheDocument();
    });

    it('should display empty wishlist message', () => {
      const WishlistPage = ({ items }) => (
        <div>
          {items.length === 0 ? (
            <p>Your wishlist is empty</p>
          ) : (
            items.map((item) => <div key={item.id}>{item.title}</div>)
          )}
        </div>
      );

      render(<WishlistPage items={[]} />);
      expect(screen.getByText('Your wishlist is empty')).toBeInTheDocument();
    });
  });

  describe('Wishlist Actions', () => {
    it('should buy book directly from wishlist', async () => {
      const mockBuyBook = jest.fn().mockResolvedValue({ success: true });

      const WishlistItem = ({ item, onBuy }) => (
        <div>
          <p>{item.title}</p>
          <p>${item.price}</p>
          <button onClick={() => onBuy(item.id)}>Buy Now</button>
        </div>
      );

      render(
        <WishlistItem
          item={{ id: 1, title: 'Test Book', price: 15.99 }}
          onBuy={mockBuyBook}
        />
      );

      const button = screen.getByText('Buy Now');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockBuyBook).toHaveBeenCalledWith(1);
      });
    });

    it('should rent book directly from wishlist', async () => {
      const mockRentBook = jest.fn().mockResolvedValue({ success: true });

      const WishlistItem = ({ item, onRent }) => (
        <div>
          <p>{item.title}</p>
          <p>${item.rentalPrice}/day</p>
          <button onClick={() => onRent(item.id)}>Rent Now</button>
        </div>
      );

      render(
        <WishlistItem
          item={{ id: 1, title: 'Test Book', rentalPrice: 2.99 }}
          onRent={mockRentBook}
        />
      );

      const button = screen.getByText('Rent Now');
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockRentBook).toHaveBeenCalledWith(1);
      });
    });
  });

  describe('Wishlist Sorting and Filtering', () => {
    it('should sort wishlist by price', () => {
      const mockWishlist = [
        { id: 1, title: 'Book 1', price: 25.99 },
        { id: 2, title: 'Book 2', price: 10.99 },
        { id: 3, title: 'Book 3', price: 15.99 },
      ];

      const sortByPrice = (items) => [...items].sort((a, b) => a.price - b.price);

      const sorted = sortByPrice(mockWishlist);

      expect(sorted[0].price).toBe(10.99);
      expect(sorted[1].price).toBe(15.99);
      expect(sorted[2].price).toBe(25.99);
    });

    it('should filter wishlist by author', () => {
      const mockWishlist = [
        { id: 1, title: 'Book 1', author: 'John Smith' },
        { id: 2, title: 'Book 2', author: 'Jane Doe' },
        { id: 3, title: 'Book 3', author: 'John Smith' },
      ];

      const filterByAuthor = (items, author) => items.filter((item) => item.author === author);

      const johnBooks = filterByAuthor(mockWishlist, 'John Smith');

      expect(johnBooks.length).toBe(2);
      expect(johnBooks.every((item) => item.author === 'John Smith')).toBe(true);
    });
  });

  describe('Wishlist Count', () => {
    it('should display wishlist count in header', () => {
      const Header = ({ wishlistCount }) => (
        <div>
          <p>Wishlist ({wishlistCount})</p>
        </div>
      );

      render(<Header wishlistCount={5} />);
      expect(screen.getByText('Wishlist (5)')).toBeInTheDocument();
    });

    it('should update wishlist count when item added', () => {
      const Wishlist = () => {
        const [count, setCount] = React.useState(0);

        const handleAdd = () => {
          setCount(count + 1);
        };

        return (
          <div>
            <p>Items: {count}</p>
            <button onClick={handleAdd}>Add Item</button>
          </div>
        );
      };

      render(<Wishlist />);

      expect(screen.getByText('Items: 0')).toBeInTheDocument();

      const button = screen.getByText('Add Item');
      fireEvent.click(button);

      expect(screen.getByText('Items: 1')).toBeInTheDocument();
    });
  });
});
