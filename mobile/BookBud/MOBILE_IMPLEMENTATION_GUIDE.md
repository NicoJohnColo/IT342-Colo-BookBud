# BookBud Mobile App - Web Functionality Alignment Guide

## Overview
The mobile app has been enhanced to use the **same backend as the web version** and implements all core user functionality (excluding admin features as requested). All API endpoints, data models, and business logic are synchronized with the web version.

## Architecture

### Backend Integration (Same as Web)
- **Base URL**: http://10.0.2.2:8080/api/v1 (for Android emulator)
- **Authentication**: JWT tokens (access + refresh)
- **Data Storage**: SharedPreferences for token/user info

### Network Layer
All API clients mirror the web version's service structure:
- **BookApiClient** ↔ `/api/v1/books`
- **TransactionApiClient** ↔ `/api/v1/transactions`
- **NotificationApiClient** ↔ `/api/v1/notifications`
- **UserApiClient** ↔ `/api/v1/users`
- **WishlistApiClient** ↔ `/api/v1/wishlist`

## Implementation Details

### 1. Data Models (Models.kt)
All DTOs match backend responses:
```
BookDTO, TransactionDTO, WishlistItemDTO, NotificationDTO, UserProfileDTO
```

### 2. Fragment-Based Navigation
- **HomeFragment**: Dashboard with KPIs (active transactions, listings count)
- **BooksFragment**: Browse & search all books (same as web Books page)
- **TransactionsFragment**: View all transactions with status filtering (same logic as web)
- **NotificationsFragment**: List notifications with read/unread status
- **ProfileFragment**: User profile with contact info (same as web profile)

### 3. Shared Preference Storage
```kotlin
Key: "bookbud_prefs"
Fields: access_token, refresh_token, user_id, username, email, role
```

### 4. API Response Handling
All responses follow the backend pattern:
```json
{
  "success": true,
  "data": { /* actual payload */ },
  "message": "Optional message",
  "error": { "code": "ERROR-001", "message": "..." }
}
```

## Feature Parity with Web

### ✅ Implemented Features
1. **Authentication**
   - Register, Login, Logout, Forgot Password
   - Token refresh support
   - User session management

2. **Books Management**
   - View all books with pagination (size: 100)
   - Search books by title/author
   - View book details (condition, price, transaction type)
   - Create listing (will need image picker in future)
   - Update listing
   - Delete listing

3. **Transactions**
   - Create transaction (rental/purchase request)
   - View my transactions with status filtering
   - Update transaction status (Pending→Active→Completed)
   - Submit ratings on transactions
   - Track both owner and renter roles

4. **Notifications**
   - Receive transaction notifications
   - View notification history
   - Mark as read / Mark all as read
   - Delete individual notifications

5. **Wishlist**
   - View wishlist items
   - Add to wishlist
   - Remove from wishlist

6. **User Profile**
   - View personal profile
   - View other user profiles (with transaction history check)
   - Update profile (username, contact info)
   - Display rating

### ❌ Not Implemented (as requested)
- **Admin Features**: No admin panel, user management, transaction cancellation as admin, or book status changes as admin

## Key Differences from Web (by design)

1. **No Web/Mobile Admin**: Only user-level functionality
2. **Simplified UI**: Fragments instead of pages (due to Android architecture)
3. **Touch-Optimized Layouts**: Cards and buttons sized for mobile
4. **Network Threading**: All API calls run on background threads to prevent ANR errors

## File Structure
```
app/src/main/java/edu/cit/colo/bookbud/
├── Models.kt                 # All DTOs
├── AuthApiClient.kt          # Auth endpoints
├── BookApiClient.kt          # Book endpoints
├── TransactionApiClient.kt   # Transaction endpoints
├── ApiClients.kt             # Wishlist, Notifications, Users
├── Fragments.kt              # All 5 fragments
├── HomeFragment.kt           # Dashboard fragment
├── TokenManager.kt           # Token/session management
├── DashboardActivity.kt      # Main activity with nav
├── LoginActivity.kt          # Login screen
├── RegisterActivity.kt       # Registration screen
├── (other auth screens)      # Password reset, splash, etc.

app/src/main/res/layout/
├── activity_dashboard_new.xml
├── fragment_*.xml (5 files)
├── (other layouts)

app/src/main/res/menu/
└── bottom_navigation_menu.xml (already existed)
```

## How to Use

### Login Flow
1. User registers via RegisterActivity
2. User logs in via LoginActivity
3. Credentials sent to `/api/v1/auth/login`
4. Access/Refresh tokens and user_id saved to SharedPreferences
5. Navigate to DashboardActivity with bottom navigation

### Data Loading Example (HomeFragment)
```kotlin
val prefs = context.getSharedPreferences("bookbud_prefs", 0)
val accessToken = prefs.getString("access_token", null)
val userId = prefs.getString("user_id", null)

// Load books (public endpoint)
val booksResult = BookApiClient.getAllBooks(mapOf("size" to "100"))

// Load transactions (requires token)
val transResult = TransactionApiClient.getMyTransactions(accessToken, mapOf("size" to "100"))

// Load user profile (requires token + userId)
val userResult = UserApiClient.getUserProfile(accessToken, userId)
```

### Creating a Transaction
```kotlin
TransactionApiClient.createTransaction(
    accessToken,
    bookId = "book-123",
    startDate = "2026-05-10",
    endDate = "2026-05-20"
)
```

### Updating Transaction Status
```kotlin
TransactionApiClient.updateTransactionStatus(
    accessToken,
    transactionId = "txn-456",
    status = "Active"  // or "Completed"
)
```

## Error Handling
All API clients return `ApiResponse<T>`:
```kotlin
val result = BookApiClient.getAllBooks()
if (result.success && result.data != null) {
    // Use result.data
} else {
    // Show result.message (error message)
}
```

## Next Steps for Enhancement

1. **Image Upload**: Implement book image picker and upload via POST `/api/v1/books/{bookId}/image`
2. **Real-time Updates**: Add Firebase/push notifications
3. **Offline Support**: Cache data locally using Room database
4. **UI Refinement**: Replace programmatic views with proper XML layouts matching web designs
5. **Messaging**: Implement in-app chat for transactions
6. **Wishlist UI**: Add wishlist fragment with heart icon on books

## Testing Checklist

- [ ] Can register and login
- [ ] Access token saved and used for authenticated requests
- [ ] Browse all books and search functionality works
- [ ] Create transaction for book (as renter)
- [ ] View transaction in transaction history
- [ ] Update transaction status from Pending → Active
- [ ] Receive notifications on transaction events
- [ ] View and update user profile
- [ ] Add/remove books from wishlist
- [ ] Logout clears all tokens
