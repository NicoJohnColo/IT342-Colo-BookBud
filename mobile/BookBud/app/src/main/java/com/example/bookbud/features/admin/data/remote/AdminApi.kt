package com.example.bookbud.features.admin.data.remote

import com.example.bookbud.shared.models.AdminPlatformStatsDTO
import com.example.bookbud.shared.models.ApiResponse
import com.example.bookbud.shared.models.BookDTO
import com.example.bookbud.shared.models.NotificationDTO
import com.example.bookbud.shared.models.PaginatedResponse
import com.example.bookbud.shared.models.TransactionDTO
import com.example.bookbud.shared.models.UserProfileDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface AdminApi {
    @GET("/api/v1/admin/stats")
    suspend fun getPlatformStats(@Header("Authorization") token: String): ApiResponse<AdminPlatformStatsDTO>

    @GET("/api/v1/admin/books")
    suspend fun getBooks(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PaginatedResponse<BookDTO>>

    @PUT("/api/v1/admin/books/{bookId}/status")
    suspend fun updateBookStatus(
        @Path("bookId") bookId: String,
        @Header("Authorization") token: String,
        @Body status: String
    ): ApiResponse<BookDTO>

    @DELETE("/api/v1/admin/books/{bookId}")
    suspend fun deleteBook(
        @Path("bookId") bookId: String,
        @Header("Authorization") token: String
    ): ApiResponse<Void>

    @GET("/api/v1/admin/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PaginatedResponse<UserProfileDTO>>

    @PUT("/api/v1/admin/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Body status: String
    ): ApiResponse<UserProfileDTO>

    @GET("/api/v1/admin/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PaginatedResponse<TransactionDTO>>

    @PUT("/api/v1/admin/transactions/{transactionId}/cancel")
    suspend fun cancelTransaction(
        @Path("transactionId") transactionId: String,
        @Header("Authorization") token: String
    ): ApiResponse<TransactionDTO>

    @GET("/api/v1/admin/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PaginatedResponse<NotificationDTO>>
}
