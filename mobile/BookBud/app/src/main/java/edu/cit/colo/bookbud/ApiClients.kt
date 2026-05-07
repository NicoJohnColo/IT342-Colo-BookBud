package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object WishlistApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/wishlist"

    fun getMyWishlist(accessToken: String): ApiResponse<List<WishlistItemDTO>> {
        return try {
            val response = get(BASE_URL, accessToken)
            @Suppress("UNCHECKED_CAST")
            parseResponse(response) as ApiResponse<List<WishlistItemDTO>>
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun addToWishlist(accessToken: String, bookId: String): ApiResponse<WishlistItemDTO> {
        return try {
            val body = JSONObject().apply {
                put("bookId", bookId)
            }
            val response = post(BASE_URL, body, accessToken)
            @Suppress("UNCHECKED_CAST")
            parseResponse(response) as ApiResponse<WishlistItemDTO>
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun removeFromWishlist(accessToken: String, wishlistId: String): ApiResponse<String> {
        return try {
            val response = delete("$BASE_URL/$wishlistId", accessToken)
            ApiResponse(success = true, data = "Removed", message = "Success")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    private fun get(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun post(url: String, body: JSONObject, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun delete(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun parseResponse(json: String): ApiResponse<Any> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.opt("data")
            val message = root.optString("message")

            if (success && data != null) {
                ApiResponse(success = true, data = data, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }
}

object NotificationApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/notifications"

    fun getMyNotifications(accessToken: String): ApiResponse<List<NotificationDTO>> {
        return try {
            val response = get(BASE_URL, accessToken)
            parseResponse<List<NotificationDTO>>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun markAsRead(accessToken: String, notificationId: String): ApiResponse<NotificationDTO> {
        return try {
            val body = JSONObject()
            val response = post("$BASE_URL/$notificationId/mark-read", body, accessToken)
            parseResponse<NotificationDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun markAllAsRead(accessToken: String): ApiResponse<String> {
        return try {
            val body = JSONObject()
            val response = post("$BASE_URL/mark-all-read", body, accessToken)
            ApiResponse(success = true, data = "Success", message = "All marked as read")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun deleteNotification(accessToken: String, notificationId: String): ApiResponse<String> {
        return try {
            delete("$BASE_URL/$notificationId", accessToken)
            ApiResponse(success = true, data = "Deleted", message = "Success")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    private fun get(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun post(url: String, body: JSONObject, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun delete(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private inline fun <reified T> parseResponse(json: String): ApiResponse<T> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.opt("data")
            val message = root.optString("message")

            if (success && data != null) {
                // Handle array of notifications
                val parsed: T? = when (T::class) {
                    List::class -> {
                        val notifications = mutableListOf<NotificationDTO>()
                        if (data is org.json.JSONArray) {
                            for (i in 0 until data.length()) {
                                notifications.add(parseNotificationDTO(data.getJSONObject(i)))
                            }
                        }
                        notifications as? T
                    }
                    NotificationDTO::class -> {
                        parseNotificationDTO(data as org.json.JSONObject) as? T
                    }
                    else -> data as? T
                }
                ApiResponse(success = true, data = parsed, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parseNotificationDTO(json: org.json.JSONObject): NotificationDTO {
        return NotificationDTO(
            notificationId = json.optString("notificationId"),
            userId = json.optString("userId"),
            message = json.optString("message"),
            type = json.optString("type"),
            isRead = json.optBoolean("isRead", false),
            createdAt = json.optString("createdAt")
        )
    }
}

object UserApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/users"

    fun getUserProfile(accessToken: String, userId: String): ApiResponse<UserProfileDTO> {
        return try {
            val response = get("$BASE_URL/$userId", accessToken)
            parseResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updateUserProfile(
        accessToken: String,
        userId: String,
        userData: UpdateUserRequest
    ): ApiResponse<UserProfileDTO> {
        return try {
            val body = JSONObject().apply {
                userData.username?.let { put("username", it) }
                userData.facebookUrl?.let { if (it.isNotEmpty()) put("facebookUrl", it) }
                userData.messenger?.let { if (it.isNotEmpty()) put("messenger", it) }
                userData.mobileNumber?.let { if (it.isNotEmpty()) put("mobileNumber", it) }
            }
            val response = put("$BASE_URL/$userId", body, accessToken)
            parseResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    private fun get(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun put(url: String, body: JSONObject, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doOutput = true

        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun parseResponse(json: String): ApiResponse<UserProfileDTO> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val user = UserProfileDTO(
                    userId = data.optString("userId"),
                    username = data.optString("username"),
                    email = data.optString("email"),
                    role = data.optString("role"),
                    rating = data.optString("rating"),
                    createdAt = data.optString("createdAt"),
                    facebookUrl = data.optString("facebookUrl"),
                    messenger = data.optString("messenger"),
                    mobileNumber = data.optString("mobileNumber"),
                    accountStatus = data.optString("accountStatus")
                )
                ApiResponse(success = true, data = user, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }
}
