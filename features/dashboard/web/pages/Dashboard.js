import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import LogoImg from '../../components/imgs/logo.png';
import { FaHome, FaGlobe, FaListUl, FaFileAlt, FaWallet, FaHeart, FaBell, FaUser, FaSignOutAlt, FaSearch } from 'react-icons/fa';

import bookService from '../../services/bookService';
import transactionService from '../../services/transactionService';
import wishlistService from '../../services/wishlistService';
import notificationService from '../../services/notificationService';
import userService from '../../services/userService';
import adminService from '../../services/adminService';
import paymentService from '../../services/paymentService';

import OverviewPage from '../user/OverviewPage';
import BrowsePage from '../user/BrowsePage';
import ListingsPage from '../user/ListingsPage';
import TransactionsPage from '../user/TransactionsPage';
import PaymentPage from '../user/PaymentPage';
import WishlistPage from '../user/WishlistPage';
import NotificationsPage from '../user/NotificationsPage';
import ProfilePage from '../user/ProfilePage';
import AdminDashboardPage from '../admin/AdminDashboardPage';
import AdminBooksPage from '../admin/AdminBooksPage';
import AdminTransactionsPage from '../admin/AdminTransactionsPage';
import AdminNotificationsPage from '../admin/AdminNotificationsPage';
import AdminUsersPage from '../admin/AdminUsersPage';

import './styles/theme.css';
import './styles/layout.css';
import './styles/common.css';

const USER_NAV_ITEMS = [
  { key: 'dashboard', label: 'Overview', icon: 'FaHome' },
  { key: 'browse', label: 'Browse', icon: 'FaGlobe' },
  { key: 'listings', label: 'Listings', icon: 'FaListUl' },
  { key: 'transactions', label: 'My Transactions', icon: 'FaFileAlt' },
  { key: 'payments', label: 'Earnings & Payments', icon: 'FaWallet' },
  { key: 'wishlist', label: 'Wishlist', icon: 'FaHeart' },
  { key: 'notifications', label: 'Notifications', icon: 'FaBell' },
  { key: 'profile', label: 'My Profile', icon: 'FaUser' },
];

const ADMIN_NAV_ITEMS = [
  { key: 'admin-dashboard', label: 'Dashboard', icon: 'FaHome' },
  { key: 'admin-books', label: 'Book Management', icon: 'FaListUl' },
  { key: 'admin-transactions', label: 'Transactions', icon: 'FaFileAlt' },
  { key: 'admin-notifications', label: 'Notification Logs', icon: 'FaBell' },
  { key: 'admin-users', label: 'User Management', icon: 'FaUser' },
];

  const getIcon = (iconName) => {
    const iconMap = {
      FaHome,
      FaGlobe,
      FaListUl,
      FaFileAlt,
      FaWallet,
      FaHeart,
      FaBell,
      FaUser,
    };
    const IconComponent = iconMap[iconName];
    return IconComponent ? <IconComponent /> : null;
  };

const toArray = (value) => {
  if (Array.isArray(value)) return value;
  if (Array.isArray(value?.content)) return value.content;
  return [];
};

const readBookList = (payload) => toArray(payload?.data || payload);

const readPaginatedList = (payload) => toArray(payload);

export default function Dashboard() {
  const { user, handleLogout } = useAuth();
  const navigate = useNavigate();

  const isAdmin = String(user?.role || '').toUpperCase() === 'ADMIN';
  const navItems = isAdmin ? ADMIN_NAV_ITEMS : USER_NAV_ITEMS;

  const [currentPage, setCurrentPage] = useState(isAdmin ? 'admin-dashboard' : 'dashboard');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [listingSaving, setListingSaving] = useState(false);

  const [books, setBooks] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [wishlist, setWishlist] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [profile, setProfile] = useState(null);

  const [adminBooks, setAdminBooks] = useState([]);
  const [adminUsers, setAdminUsers] = useState([]);
  const [adminTransactions, setAdminTransactions] = useState([]);
  const [adminNotifications, setAdminNotifications] = useState([]);

  useEffect(() => {
    if (!user) {
      navigate('/');
      return;
    }
    setCurrentPage(isAdmin ? 'admin-dashboard' : 'dashboard');
  }, [user, isAdmin, navigate]);

  const loadUserData = useCallback(async () => {
    if (!user) return;

    setLoading(true);
    try {
      const [booksResult, transactionsResult, wishlistResult, notificationsResult, profileResult] = await Promise.allSettled([
        bookService.getAllBooks({ size: 100 }),
        transactionService.getMyTransactions({ size: 100 }),
        wishlistService.getMyWishlist(),
        notificationService.getMyNotifications(),
        user.userId ? userService.getUserProfile(user.userId) : Promise.resolve(null),
      ]);

      if (booksResult.status === 'fulfilled') {
        setBooks(readBookList(booksResult.value));
      }

      if (transactionsResult.status === 'fulfilled') {
        setTransactions(readPaginatedList(transactionsResult.value));
      }

      if (wishlistResult.status === 'fulfilled') {
        setWishlist(toArray(wishlistResult.value));
      }

      if (notificationsResult.status === 'fulfilled') {
        setNotifications(toArray(notificationsResult.value));
      }

      if (profileResult.status === 'fulfilled') {
        setProfile(profileResult.value);
      }
    } finally {
      setLoading(false);
    }
  }, [user]);

  const loadAdminData = useCallback(async () => {
    if (!isAdmin) return;

    const [booksResult, usersResult, transactionsResult, notificationsResult] = await Promise.allSettled([
      adminService.getBooks({ size: 100 }),
      adminService.getUsers({ size: 100 }),
      adminService.getTransactions({ size: 100 }),
      adminService.getNotifications({ size: 100 }),
    ]);

    if (booksResult.status === 'fulfilled') {
      setAdminBooks(readPaginatedList(booksResult.value));
    }
    if (usersResult.status === 'fulfilled') {
      setAdminUsers(readPaginatedList(usersResult.value));
    }
    if (transactionsResult.status === 'fulfilled') {
      setAdminTransactions(readPaginatedList(transactionsResult.value));
    }
    if (notificationsResult.status === 'fulfilled') {
      setAdminNotifications(readPaginatedList(notificationsResult.value));
    }
  }, [isAdmin]);

  useEffect(() => {
    loadUserData();
  }, [loadUserData]);

  useEffect(() => {
    loadAdminData();
  }, [loadAdminData]);

  const filteredBooks = useMemo(() => {
    if (!search.trim()) return books;
    const query = search.toLowerCase();
    return books.filter((book) => [book.title, book.author, book.genre].some((v) => String(v || '').toLowerCase().includes(query)));
  }, [books, search]);

  const myListings = useMemo(
    () => books.filter((book) => String(book.ownerId || '') === String(user?.userId || '')),
    [books, user]
  );

  // Debug logging for data - moved after myListings definition
  useEffect(() => {
    if (!user) return;
    console.log('Dashboard Data Summary:', {
      userId: user?.userId,
      username: user?.username,
      booksCount: books.length,
      myListingsCount: myListings.length,
      transactionsCount: transactions.length,
      wishlistCount: wishlist.length,
    });
  }, [books, myListings, transactions, wishlist, user]);

  const unreadCount = useMemo(() => notifications.filter((n) => !n.isRead).length, [notifications]);

  const onLogout = async () => {
    await handleLogout();
    navigate('/');
  };

  const onMarkRead = async (notificationId) => {
    try {
      await notificationService.markAsRead(notificationId);
      setNotifications((prev) => prev.map((n) => (n.notificationId === notificationId ? { ...n, isRead: true } : n)));
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onDeleteNotification = async (notificationId) => {
    try {
      await notificationService.deleteNotification(notificationId);
      setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onRemoveWishlist = async (wishlistId) => {
    try {
      await wishlistService.removeFromWishlist(wishlistId);
      setWishlist((prev) => prev.filter((item) => item.wishlistId !== wishlistId));
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onUpdateProfile = async (formData) => {
    if (!user?.userId) return;
    try {
      await userService.updateUserProfile(user.userId, formData);
      await loadUserData();
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onCreateListing = async (payload, imageFile) => {
    setListingSaving(true);
    try {
      const response = await bookService.createBook(payload);
      const created = response?.data || null;

      if (!created?.bookId) {
        await loadUserData();
        return null;
      }

      let finalBook = created;
      if (imageFile) {
        try {
          const imageResponse = await bookService.uploadBookImage(created.bookId, imageFile);
          finalBook = imageResponse?.data || created;
        } catch {
          // Keep listing created even if image upload fails.
        }
      }

      setBooks((prev) => [finalBook, ...prev.filter((book) => book.bookId !== finalBook.bookId)]);
      return finalBook;
    } finally {
      setListingSaving(false);
    }
  };

  const onUpdateListing = async (bookId, payload, imageFile) => {
    setListingSaving(true);
    try {
      const response = await bookService.updateBook(bookId, payload);
      const updated = response?.data || null;

      if (!updated?.bookId) {
        await loadUserData();
        return null;
      }

      let finalBook = updated;
      if (imageFile) {
        try {
          const imageResponse = await bookService.uploadBookImage(bookId, imageFile);
          finalBook = imageResponse?.data || updated;
        } catch {
          // Keep metadata update even if image upload fails.
        }
      }

      setBooks((prev) => prev.map((book) => (book.bookId === finalBook.bookId ? finalBook : book)));
      return finalBook;
    } finally {
      setListingSaving(false);
    }
  };

  const onCreateTransaction = useCallback(
    async (payload) => {
      const created = await transactionService.createTransaction(payload);
      await loadUserData();
      return created;
    },
    [loadUserData]
  );

  const onDeleteListing = async (bookId) => {
    try {
      await bookService.deleteBook(bookId);
      setBooks((prev) => prev.filter((book) => book.bookId !== bookId));
    } catch {
      // Keep UI stable if API call fails.
    }
  };

  const onUpdateTransactionStatus = useCallback(
    async (transactionId, status) => {
      const updated = await transactionService.updateTransactionStatus(transactionId, status);
      await loadUserData();
      // Force refresh all related data
      window.dispatchEvent(new CustomEvent('refreshPayments'));
      window.dispatchEvent(new CustomEvent('refreshDashboard'));
      return updated;
    },
    [loadUserData]
  );

  const onSubmitRating = useCallback(
    async (transactionId, rating) => {
      const result = await transactionService.submitRating(transactionId, rating);
      await loadUserData();
      return result;
    },
    [loadUserData]
  );

  const renderPage = () => {
    switch (currentPage) {
      case 'dashboard':
        return (
          <OverviewPage
            user={user}
            books={filteredBooks}
            myListings={myListings}
            transactions={transactions}
            onNavigate={setCurrentPage}
            currentUserId={user?.userId}
            onCreateTransaction={onCreateTransaction}
            wishlist={wishlist}
            onWishlistChange={loadUserData}
          />
        );
      case 'browse':
        return (
          <BrowsePage
            books={filteredBooks}
            currentUserId={user?.userId}
            onCreateTransaction={onCreateTransaction}
            wishlist={wishlist}
            onWishlistChange={loadUserData}
          />
        );
      case 'listings':
        return (
          <ListingsPage
            listings={myListings}
            saving={listingSaving}
            onCreateListing={onCreateListing}
            onUpdateListing={onUpdateListing}
            onDeleteListing={onDeleteListing}
          />
        );
      case 'transactions':
        return (
          <TransactionsPage
            transactions={transactions}
            onUpdateStatus={onUpdateTransactionStatus}
            onSubmitRating={onSubmitRating}
          />
        );
      case 'payments':
        return <PaymentPage transactions={transactions} books={books} />;
      case 'wishlist':
        return <WishlistPage wishlist={wishlist} onRemove={onRemoveWishlist} />;
      case 'notifications':
        return (
          <NotificationsPage
            notifications={notifications}
            onMarkRead={onMarkRead}
            onMarkAllRead={onMarkAllRead}
            onDelete={onDeleteNotification}
          />
        );
      case 'profile':
        return (
          <ProfilePage
            user={user}
            profile={profile}
            myListingsCount={myListings.length}
            transactionsCount={transactions.length}
            onUpdateProfile={onUpdateProfile}
          />
        );
      case 'admin-dashboard':
        return (
          <AdminDashboardPage
            books={adminBooks}
            users={adminUsers}
            transactions={adminTransactions}
            notifications={adminNotifications}
          />
        );
      case 'admin-books':
        return <AdminBooksPage books={adminBooks} />;
      case 'admin-transactions':
        return <AdminTransactionsPage transactions={adminTransactions} />;
      case 'admin-notifications':
        return <AdminNotificationsPage notifications={adminNotifications} />;
      case 'admin-users':
        return <AdminUsersPage users={adminUsers} />;
      default:
        return isAdmin ? (
          <AdminDashboardPage books={adminBooks} users={adminUsers} transactions={adminTransactions} notifications={adminNotifications} />
        ) : (
          <OverviewPage
            user={user}
            books={filteredBooks}
            myListings={myListings}
            transactions={transactions}
            onNavigate={setCurrentPage}
            currentUserId={user?.userId}
            onCreateTransaction={onCreateTransaction}
          />
        );
    }
  };

  return (
    <div className="dashboard-shell">
      <div className="app-layout">
        <aside className="sidebar">
          <div className="sidebar-logo">
            <img src={LogoImg} alt="BookBud" className="logo-image" />
          </div>
          <nav className="sidebar-nav">
            {navItems.map((item) => (
              <button key={item.key} className={`nav-item ${currentPage === item.key ? 'active' : ''}`} onClick={() => setCurrentPage(item.key)}>
                <span className="nav-icon">{getIcon(item.icon)}</span>
                <span>{item.label}</span>
                {item.key === 'notifications' && unreadCount > 0 ? (
                  <span style={{ marginLeft: 'auto', fontSize: 10, fontWeight: 700 }}>{unreadCount}</span>
                ) : null}
              </button>
            ))}
          </nav>
          <div className="sidebar-footer">
            <button className="logout-btn" onClick={onLogout}>
              <FaSignOutAlt />
              <span>Logout</span>
            </button>
          </div>
        </aside>

        <header className="header">
          <div></div>
          <div className="header-user">
            <div className="header-avatar">{String(user?.username || 'U').slice(0, 1).toUpperCase()}</div>
            <div>
              <div className="header-name">{user?.username || 'User'}</div>
              <div className="header-email">{user?.email || 'No email'}</div>
            </div>
          </div>
        </header>

        <main className="main-content">{loading ? <div className="empty-state">Loading dashboard data...</div> : renderPage()}</main>
      </div>
    </div>
  );
}
