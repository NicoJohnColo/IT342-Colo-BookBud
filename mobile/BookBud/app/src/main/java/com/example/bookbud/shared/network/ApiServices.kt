package com.example.bookbud.shared.network

import com.example.bookbud.shared.models.*
import retrofit2.http.*

// ==================== Auth API ====================
interface AuthApi {
    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @GET("/api/v1/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): ApiResponse<UserProfileDTO>
}

// ==================== Books API ====================
interface BooksApi {
    @GET("/api/v1/books")
    suspend fun getAllBooks(
        @QueryMap params: Map<String, String>
    ): ApiResponse<PaginatedResponse<BookDTO>>

    @GET("/api/v1/books/{bookId}")
    suspend fun getBook(@Path("bookId") bookId: String): ApiResponse<BookDTO>

    @POST("/api/v1/books")
    suspend fun createBook(
        @Header("Authorization") token: String,
        @Body request: CreateBookRequest
    ): ApiResponse<BookDTO>

    @PUT("/api/v1/books/{bookId}")
    suspend fun updateBook(
        @Path("bookId") bookId: String,
        @Header("Authorization") token: String,
        @Body request: CreateBookRequest
    ): ApiResponse<BookDTO>

    @DELETE("/api/v1/books/{bookId}")
    suspend fun deleteBook(
        @Path("bookId") bookId: String,
        @Header("Authorization") token: String
    ): ApiResponse<Void>

    @GET("/api/v1/books/search-external")
    suspend fun searchExternalBooks(
        @Query("q") q: String
    ): ApiResponse<List<ExternalBookDTO>>
}

// ==================== Transactions API ====================
interface TransactionsApi {
    @GET("/api/v1/transactions")
    suspend fun getMyTransactions(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<TransactionDTO>>

    @GET("/api/v1/transactions/{transactionId}")
    suspend fun getTransaction(
        @Path("transactionId") transactionId: String,
        @Header("Authorization") token: String
    ): ApiResponse<TransactionDTO>

    @POST("/api/v1/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: CreateTransactionRequest
    ): ApiResponse<TransactionDTO>

    @PATCH("/api/v1/transactions/{transactionId}/status")
    suspend fun updateTransactionStatus(
        @Path("transactionId") transactionId: String,
        @Header("Authorization") token: String,
        @Body request: UpdateTransactionStatusRequest
    ): ApiResponse<TransactionDTO>

    @POST("/api/v1/transactions/{transactionId}/rating")
    suspend fun submitRating(
        @Path("transactionId") transactionId: String,
        @Header("Authorization") token: String,
        @Body request: SubmitRatingRequest
    ): ApiResponse<TransactionDTO>
}

// ==================== Users API ====================
interface UsersApi {
    @GET("/api/v1/users/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String? = null
    ): ApiResponse<UserProfileDTO>

    @PUT("/api/v1/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserProfileRequest
    ): ApiResponse<UserProfileDTO>
}

// ==================== Wishlist API ====================
interface WishlistApi {
    @GET("/api/v1/wishlist")
    suspend fun getMyWishlist(
        @Header("Authorization") token: String
    ): ApiResponse<List<WishlistItemDTO>>

    @POST("/api/v1/wishlist")
    suspend fun addToWishlist(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): ApiResponse<WishlistItemDTO>

    @DELETE("/api/v1/wishlist/{wishlistId}")
    suspend fun removeFromWishlist(
        @Path("wishlistId") wishlistId: String,
        @Header("Authorization") token: String
    ): ApiResponse<Void>
}

// ==================== Notifications API ====================
interface NotificationsApi {
    @GET("/api/v1/notifications")
    suspend fun getMyNotifications(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): ApiResponse<List<NotificationDTO>>

    @PATCH("/api/v1/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId") notificationId: String,
        @Header("Authorization") token: String
    ): ApiResponse<NotificationDTO>

    @PATCH("/api/v1/notifications/mark-all-read")
    suspend fun markAllAsRead(
        @Header("Authorization") token: String
    ): ApiResponse<Void>

    @DELETE("/api/v1/notifications/{notificationId}")
    suspend fun deleteNotification(
        @Path("notificationId") notificationId: String,
        @Header("Authorization") token: String
    ): ApiResponse<Void>
}

// ==================== Payments API ====================
interface PaymentsApi {
    @GET("/api/v1/payments/stats")
    suspend fun getEarningsSummary(
        @Header("Authorization") token: String
    ): ApiResponse<EarningsSummaryDTO>

    @GET("/api/v1/payments/received")
    suspend fun getPaymentsReceived(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<PaymentDTO>>

    @GET("/api/v1/payments/made")
    suspend fun getPaymentsMade(
        @Header("Authorization") token: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<PaymentDTO>>

    @POST("/api/v1/payments/initiate-stripe")
    suspend fun createPaymentIntent(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): ApiResponse<CreatePaymentIntentResponse>

    @POST("/api/v1/payments/confirm-stripe")
    suspend fun confirmPayment(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): ApiResponse<Void?>
}
