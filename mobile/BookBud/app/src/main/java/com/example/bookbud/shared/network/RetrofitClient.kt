package com.example.bookbud.shared.network

import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ==================== Retrofit Client ====================
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    fun create(preferencesManager: com.example.bookbud.shared.storage.PreferencesManager): Retrofit {
        return retrofit
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val booksApi: BooksApi by lazy { retrofit.create(BooksApi::class.java) }
    val transactionsApi: TransactionsApi by lazy { retrofit.create(TransactionsApi::class.java) }
    val usersApi: UsersApi by lazy { retrofit.create(UsersApi::class.java) }
    val wishlistApi: WishlistApi by lazy { retrofit.create(WishlistApi::class.java) }
    val notificationsApi: NotificationsApi by lazy { retrofit.create(NotificationsApi::class.java) }
    val paymentsApi: PaymentsApi by lazy { retrofit.create(PaymentsApi::class.java) }
    val adminApi: com.example.bookbud.features.admin.data.remote.AdminApi by lazy { retrofit.create(com.example.bookbud.features.admin.data.remote.AdminApi::class.java) }
}

// ==================== API Clients ====================
object BookApiClient {
    suspend fun getAllBooks(params: Map<String, String> = mapOf("size" to "100")): List<BookDTO>? {
        return try {
            RetrofitClient.booksApi.getAllBooks(params).data?.content
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchExternalBooks(q: String): List<ExternalBookDTO>? {
        return try {
            RetrofitClient.booksApi.searchExternalBooks(q).data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createBook(token: String, request: CreateBookRequest): BookDTO? {
        return try {
            RetrofitClient.booksApi.createBook("Bearer $token", request).data
        } catch (e: Exception) {
            null
        }
    }
}

object TransactionApiClient {
    suspend fun getMyTransactions(token: String, params: Map<String, String> = mapOf("size" to "100")): List<TransactionDTO> {
        return try {
            RetrofitClient.transactionsApi.getMyTransactions("Bearer $token", params).data?.content ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTransaction(token: String, request: CreateTransactionRequest): TransactionDTO? {
        return try {
            RetrofitClient.transactionsApi.createTransaction("Bearer $token", request).data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateStatus(token: String, transactionId: String, status: String): TransactionDTO? {
        return try {
            RetrofitClient.transactionsApi.updateTransactionStatus(transactionId, "Bearer $token", UpdateTransactionStatusRequest(status)).data
        } catch (e: Exception) {
            null
        }
    }
}

object UserApiClient {
    suspend fun getUserProfile(token: String, userId: String): UserProfileDTO? {
        return try {
            RetrofitClient.usersApi.getUserProfile(userId, "Bearer $token").data
        } catch (e: Exception) {
            null
        }
    }
}

object WishlistApiClient {
    suspend fun getMyWishlist(token: String): List<WishlistItemDTO> {
        return try {
            RetrofitClient.wishlistApi.getMyWishlist("Bearer $token").data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addToWishlist(token: String, bookId: String): WishlistItemDTO? {
        return try {
            RetrofitClient.wishlistApi.addToWishlist("Bearer $token", mapOf("bookId" to bookId)).data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun removeFromWishlist(token: String, wishlistId: String) {
        try {
            RetrofitClient.wishlistApi.removeFromWishlist(wishlistId, "Bearer $token")
        } catch (e: Exception) {}
    }
}

object NotificationApiClient {
    suspend fun getMyNotifications(token: String, params: Map<String, String> = mapOf("size" to "100")): List<NotificationDTO> {
        return try {
            RetrofitClient.notificationsApi.getMyNotifications("Bearer $token", params).data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markAsRead(token: String, notificationId: String) {
        try {
            RetrofitClient.notificationsApi.markAsRead(notificationId, "Bearer $token")
        } catch (e: Exception) {}
    }

    suspend fun markAllAsRead(token: String) {
        try {
            RetrofitClient.notificationsApi.markAllAsRead("Bearer $token")
        } catch (e: Exception) {}
    }

    suspend fun deleteNotification(token: String, notificationId: String) {
        try {
            RetrofitClient.notificationsApi.deleteNotification(notificationId, "Bearer $token")
        } catch (e: Exception) {}
    }
}

object PaymentApiClient {
    suspend fun getEarningsSummary(token: String): EarningsSummaryDTO? {
        return try {
            RetrofitClient.paymentsApi.getEarningsSummary("Bearer $token").data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPaymentsReceived(token: String, params: Map<String, String> = mapOf("size" to "100")): List<PaymentDTO> {
        return try {
            RetrofitClient.paymentsApi.getPaymentsReceived("Bearer $token", params).data?.content ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createPaymentIntent(token: String, transactionId: String): CreatePaymentIntentResponse? {
        return try {
            RetrofitClient.paymentsApi.createPaymentIntent("Bearer $token", mapOf("transactionId" to transactionId)).data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun confirmPayment(token: String, transactionId: String): Boolean {
        return try {
            RetrofitClient.paymentsApi.confirmPayment("Bearer $token", mapOf("transactionId" to transactionId))
            true
        } catch (e: Exception) {
            false
        }
    }
}
