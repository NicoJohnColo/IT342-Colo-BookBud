package com.example.bookbud.shared.models

import com.google.gson.annotations.SerializedName

// ==================== API Response Wrapper ====================
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: ErrorDetail?
)

data class ErrorDetail(
    val code: String?,
    val message: String?
)

// ==================== Auth DTOs ====================
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("user_id")
    val userId: String,
    val username: String,
    val email: String,
    val role: String?
)

// ==================== User DTOs ====================
data class UserProfileDTO(
    val userId: String,
    val username: String,
    val email: String? = null,
    val rating: Double? = null,
    val createdAt: String? = null,
    val facebookUrl: String? = null,
    val messenger: String? = null,
    val mobileNumber: String? = null,
    val role: String? = null,
    val accountStatus: String? = null
)

data class AdminPlatformStatsDTO(
    val totalRevenue: Double,
    val successfulPayments: Long,
    val completedTransactions: Long
)

data class UpdateUserProfileRequest(
    val username: String?,
    val facebookUrl: String?,
    val messenger: String?,
    val mobileNumber: String?
)

// ==================== Book DTOs ====================
data class BookDTO(
    val bookId: String,
    val title: String,
    val author: String,
    val genre: String?,
    val condition: String?,
    val transactionType: String?, // "Rent", "Sale", "Both"
    val status: String?, // "Available", "Sold", "Rented"
    val priceRent: Double?,
    val priceSale: Double?,
    val description: String?,
    val imageUrl: String?,
    val ownerId: String,
    val ownerUsername: String?,
    val createdAt: String?
)

data class CreateBookRequest(
    val title: String,
    val author: String,
    val genre: String,
    val condition: String,
    val transactionType: String,
    val priceRent: Double?,
    val priceSale: Double?,
    val description: String?
)

// ==================== Transaction DTOs ====================
data class TransactionDTO(
    val transactionId: String,
    val bookId: String,
    val bookTitle: String?,
    val ownerId: String,
    val ownerUsername: String?,
    val userId: String,
    val renterUsername: String?,
    val status: String?, // "Pending", "Active", "Completed", "Cancelled"
    val paymentStatus: String?, // "Pending", "Successful", "Failed"
    val paymentMethod: String?,
    val amount: Double,
    val startDate: String?,
    val endDate: String?,
    val createdAt: String?,
    val paymentDate: String?,
    val userRole: String?,
    val ownerRated: Boolean?,
    val renterRated: Boolean?
)

data class CreateTransactionRequest(
    val bookId: String,
    val startDate: String?,
    val endDate: String?,
    val paymentMethod: String? = "Cash"
)

data class UpdateTransactionStatusRequest(
    val status: String // "Active", "Completed", "Cancelled"
)

data class SubmitRatingRequest(
    val rating: Int,
    val comment: String? = null
)

// ==================== Wishlist DTOs ====================
data class WishlistItemDTO(
    val wishlistId: String,
    val bookId: String,
    val book: BookDTO?,
    val userId: String,
    val createdAt: String?
)

// ==================== Notification DTOs ====================
data class NotificationDTO(
    val notificationId: String,
    val userId: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String?
)

data class MarkAsReadRequest(
    val isRead: Boolean = true
)

// ==================== Payment DTOs ====================
data class PaymentDTO(
    val paymentId: String,
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentStatus: String, // "Pending", "Paid", "Failed"
    val paymentDate: String?
)

data class CreatePaymentIntentRequest(
    val transactionId: String,
    val amount: Double
)

data class CreatePaymentIntentResponse(
    val clientSecret: String
)

data class ConfirmPaymentRequest(
    val transactionId: String,
    val paymentIntentId: String
)

// ==================== Earnings DTOs ====================
data class EarningsSummaryDTO(
    val totalEarnings: Double,
    val pendingPayments: Int,
    val successfulPayments: Int
)

// ==================== Pagination ====================
data class PaginatedResponse<T>(
    val content: List<T>?,
    val totalElements: Int?,
    val totalPages: Int?,
    val currentPage: Int?,
    val pageSize: Int?
)

// ==================== External Book DTOs ====================
data class ExternalBookDTO(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val categories: List<String>?
)
