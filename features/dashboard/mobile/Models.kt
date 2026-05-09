package edu.cit.colo.bookbud

// Book Models
data class BookDTO(
    val bookId: String? = null,
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val condition: String? = null, // Like New, Good, Fair, Poor
    val transactionType: String? = null, // Rent, Sale, Both
    val priceRent: Double? = null,
    val priceSale: Double? = null,
    val status: String? = null, // Available, Rented, Unavailable
    val ownerId: String? = null,
    val ownerUsername: String? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateBookRequest(
    val title: String,
    val author: String,
    val genre: String,
    val description: String? = null,
    val condition: String,
    val transactionType: String,
    val priceRent: Double? = null,
    val priceSale: Double? = null
)

data class UpdateBookRequest(
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val condition: String? = null,
    val transactionType: String? = null,
    val priceRent: Double? = null,
    val priceSale: Double? = null,
    val status: String? = null
)

// Transaction Models
data class TransactionDTO(
    val transactionId: String? = null,
    val bookId: String? = null,
    val bookTitle: String? = null,
    val userId: String? = null,
    val renterUsername: String? = null,
    val ownerId: String? = null,
    val ownerUsername: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val amount: Double? = null,
    val status: String? = null, // Pending, Active, Completed, Cancelled
    val paymentMethod: String? = null,
    val paymentStatus: String? = null, // Successful, Failed, Pending
    val paymentDate: String? = null,
    val userRole: String? = null, // Owner or Renter
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val ownerRated: Boolean? = null,
    val renterRated: Boolean? = null
)

data class CreateTransactionRequest(
    val bookId: String,
    val startDate: String? = null,
    val endDate: String? = null
)

data class RatingRequest(
    val rating: Double
)

data class RatingResponse(
    val transactionId: String? = null,
    val ratedUserId: String? = null,
    val rating: Double? = null,
    val newAggregateRating: Double? = null
)

// Wishlist Models
data class WishlistItemDTO(
    val wishlistId: String? = null,
    val userId: String? = null,
    val bookId: String? = null,
    val book: BookDTO? = null,
    val createdAt: String? = null
)

data class WishlistResponse(
    val wishlistId: String? = null,
    val bookId: String? = null,
    val book: BookDTO? = null
)

// Notification Models
data class NotificationDTO(
    val notificationId: String? = null,
    val userId: String? = null,
    val message: String? = null,
    val type: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null
)

// User Models
data class UserProfileDTO(
    val userId: String? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String? = null,
    val rating: String? = null,
    val createdAt: String? = null,
    val facebookUrl: String? = null,
    val messenger: String? = null,
    val mobileNumber: String? = null,
    val accountStatus: String? = null
)

data class UpdateUserRequest(
    val username: String? = null,
    val facebookUrl: String? = null,
    val messenger: String? = null,
    val mobileNumber: String? = null
)

// Generic Response Wrapper
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val error: ErrorDetail? = null
)

data class ErrorDetail(
    val code: String? = null,
    val message: String? = null
)

data class PaginatedResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0
)

// External Book API Models
data class ExternalBookDTO(
    val title: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val categories: List<String>? = null,
    val imageLinks: Map<String, String>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val isbn: String? = null
)
