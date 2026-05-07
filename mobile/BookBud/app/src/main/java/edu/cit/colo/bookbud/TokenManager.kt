package edu.cit.colo.bookbud

import android.content.Context
import org.json.JSONObject

object TokenManager {
    private const val PREFS_NAME = "bookbud_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun saveUser(context: Context, userId: String, username: String, email: String, role: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)
    }

    fun getUsername(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USERNAME, null)
    }

    fun clearAllTokens(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

    fun getRole(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)
    }

    fun isAdmin(context: Context): Boolean {
        return getRole(context)?.equals("ADMIN", ignoreCase = true) == true
    }
}

object JsonParser {
    fun parseUserFromJson(json: JSONObject): UserProfileDTO? {
        return try {
            UserProfileDTO(
                userId = json.optString("userId"),
                username = json.optString("username"),
                email = json.optString("email"),
                role = json.optString("role"),
                rating = json.optString("rating"),
                createdAt = json.optString("createdAt"),
                facebookUrl = json.optString("facebookUrl"),
                messenger = json.optString("messenger"),
                mobileNumber = json.optString("mobileNumber"),
                accountStatus = json.optString("accountStatus")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseBookFromJson(json: JSONObject): BookDTO? {
        return try {
            BookDTO(
                bookId = json.optString("bookId"),
                title = json.optString("title"),
                author = json.optString("author"),
                genre = json.optString("genre"),
                description = json.optString("description"),
                condition = json.optString("condition"),
                transactionType = json.optString("transactionType"),
                priceRent = json.optDouble("priceRent"),
                priceSale = json.optDouble("priceSale"),
                status = json.optString("status"),
                ownerId = json.optString("ownerId"),
                ownerUsername = json.optString("ownerUsername"),
                imageUrl = json.optString("imageUrl"),
                createdAt = json.optString("createdAt"),
                updatedAt = json.optString("updatedAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseTransactionFromJson(json: JSONObject): TransactionDTO? {
        return try {
            TransactionDTO(
                transactionId = json.optString("transactionId"),
                bookId = json.optString("bookId"),
                bookTitle = json.optString("bookTitle"),
                userId = json.optString("userId"),
                renterUsername = json.optString("renterUsername"),
                ownerId = json.optString("ownerId"),
                ownerUsername = json.optString("ownerUsername"),
                startDate = json.optString("startDate"),
                endDate = json.optString("endDate"),
                amount = json.optDouble("amount"),
                status = json.optString("status"),
                paymentMethod = json.optString("paymentMethod"),
                paymentStatus = json.optString("paymentStatus"),
                paymentDate = json.optString("paymentDate"),
                userRole = json.optString("userRole"),
                createdAt = json.optString("createdAt"),
                updatedAt = json.optString("updatedAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseNotificationFromJson(json: JSONObject): NotificationDTO? {
        return try {
            NotificationDTO(
                notificationId = json.optString("notificationId"),
                userId = json.optString("userId"),
                message = json.optString("message"),
                type = json.optString("type"),
                isRead = json.optBoolean("isRead", false),
                createdAt = json.optString("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseWishlistFromJson(json: JSONObject): WishlistItemDTO? {
        return try {
            WishlistItemDTO(
                wishlistId = json.optString("wishlistId"),
                userId = json.optString("userId"),
                bookId = json.optString("bookId"),
                book = json.optJSONObject("book")?.let { parseBookFromJson(it) },
                createdAt = json.optString("createdAt")
            )
        } catch (e: Exception) {
            null
        }
    }
}
