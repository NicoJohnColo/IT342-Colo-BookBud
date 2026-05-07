package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AdminApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/admin"

    // ==================== BOOK MANAGEMENT ====================

    fun getAllBooks(
        accessToken: String,
        params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<BookDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/books")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }
            val response = get(urlBuilder.toString(), accessToken)
            parsePaginatedBookResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updateBookStatus(
        accessToken: String,
        bookId: String,
        status: String
    ): ApiResponse<BookDTO> {
        return try {
            val body = JSONObject().apply {
                put("status", status)
            }
            val response = put("$BASE_URL/books/$bookId/status", body, accessToken)
            parseBookResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun deleteBook(accessToken: String, bookId: String): ApiResponse<String> {
        return try {
            val response = delete("$BASE_URL/books/$bookId", accessToken)
            ApiResponse(success = true, data = "Deleted", message = "Book deleted successfully")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    // ==================== USER MANAGEMENT ====================

    fun getAllUsers(
        accessToken: String,
        params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<UserProfileDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/users")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }
            val response = get(urlBuilder.toString(), accessToken)
            parsePaginatedUserResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updateUserStatus(
        accessToken: String,
        userId: String,
        status: String
    ): ApiResponse<UserProfileDTO> {
        return try {
            val body = JSONObject().apply {
                put("status", status)
            }
            val response = put("$BASE_URL/users/$userId/status", body, accessToken)
            parseUserResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    // ==================== TRANSACTION MANAGEMENT ====================

    fun getAllTransactions(
        accessToken: String,
        params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<TransactionDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/transactions")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }
            val response = get(urlBuilder.toString(), accessToken)
            parsePaginatedTransactionResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun cancelTransaction(accessToken: String, transactionId: String): ApiResponse<TransactionDTO> {
        return try {
            val body = JSONObject()
            val response = put("$BASE_URL/transactions/$transactionId/cancel", body, accessToken)
            parseTransactionResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    // ==================== NOTIFICATION MANAGEMENT ====================

    fun getAllNotifications(
        accessToken: String,
        params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<NotificationDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/notifications")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }
            val response = get(urlBuilder.toString(), accessToken)
            parsePaginatedNotificationResponse(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    // ==================== HTTP METHODS ====================

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

    private fun delete(url: String, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    // ==================== PARSERS ====================

    private fun parseBookResponse(json: String): ApiResponse<BookDTO> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val book = parseBookDTO(data)
                ApiResponse(success = true, data = book, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parsePaginatedBookResponse(json: String): ApiResponse<PaginatedResponse<BookDTO>> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val books = mutableListOf<BookDTO>()
                val contentArray = data.optJSONArray("content")
                if (contentArray != null) {
                    for (i in 0 until contentArray.length()) {
                        books.add(parseBookDTO(contentArray.getJSONObject(i)))
                    }
                }
                val paginated = PaginatedResponse(
                    content = books,
                    page = data.optInt("page"),
                    size = data.optInt("size"),
                    totalElements = data.optLong("totalElements")
                )
                ApiResponse(success = true, data = paginated, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parseUserResponse(json: String): ApiResponse<UserProfileDTO> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val user = parseUserDTO(data)
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

    private fun parsePaginatedUserResponse(json: String): ApiResponse<PaginatedResponse<UserProfileDTO>> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val users = mutableListOf<UserProfileDTO>()
                val contentArray = data.optJSONArray("content")
                if (contentArray != null) {
                    for (i in 0 until contentArray.length()) {
                        users.add(parseUserDTO(contentArray.getJSONObject(i)))
                    }
                }
                val paginated = PaginatedResponse(
                    content = users,
                    page = data.optInt("page"),
                    size = data.optInt("size"),
                    totalElements = data.optLong("totalElements")
                )
                ApiResponse(success = true, data = paginated, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parseTransactionResponse(json: String): ApiResponse<TransactionDTO> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val txn = parseTransactionDTO(data)
                ApiResponse(success = true, data = txn, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parsePaginatedTransactionResponse(json: String): ApiResponse<PaginatedResponse<TransactionDTO>> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val transactions = mutableListOf<TransactionDTO>()
                val contentArray = data.optJSONArray("content")
                if (contentArray != null) {
                    for (i in 0 until contentArray.length()) {
                        transactions.add(parseTransactionDTO(contentArray.getJSONObject(i)))
                    }
                }
                val paginated = PaginatedResponse(
                    content = transactions,
                    page = data.optInt("page"),
                    size = data.optInt("size"),
                    totalElements = data.optLong("totalElements")
                )
                ApiResponse(success = true, data = paginated, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parsePaginatedNotificationResponse(json: String): ApiResponse<PaginatedResponse<NotificationDTO>> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                val notifications = mutableListOf<NotificationDTO>()
                val contentArray = data.optJSONArray("content")
                if (contentArray != null) {
                    for (i in 0 until contentArray.length()) {
                        notifications.add(parseNotificationDTO(contentArray.getJSONObject(i)))
                    }
                }
                val paginated = PaginatedResponse(
                    content = notifications,
                    page = data.optInt("page"),
                    size = data.optInt("size"),
                    totalElements = data.optLong("totalElements")
                )
                ApiResponse(success = true, data = paginated, message = message)
            } else {
                val error = root.optJSONObject("error")
                val errorMsg = error?.optString("message") ?: message ?: "Request failed"
                ApiResponse(success = false, message = errorMsg)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = "Parse error: ${e.message}")
        }
    }

    private fun parseBookDTO(json: JSONObject): BookDTO {
        return BookDTO(
            bookId = json.optString("bookId"),
            title = json.optString("title"),
            author = json.optString("author"),
            genre = json.optString("genre"),
            description = json.optString("description"),
            condition = json.optString("condition"),
            transactionType = json.optString("transactionType"),
            priceRent = json.optDouble("priceRent").let { if (it.isNaN()) null else it },
            priceSale = json.optDouble("priceSale").let { if (it.isNaN()) null else it },
            status = json.optString("status"),
            ownerId = json.optString("ownerId"),
            ownerUsername = json.optString("ownerUsername"),
            imageUrl = json.optString("imageUrl"),
            createdAt = json.optString("createdAt"),
            updatedAt = json.optString("updatedAt")
        )
    }

    private fun parseUserDTO(json: JSONObject): UserProfileDTO {
        return UserProfileDTO(
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
    }

    private fun parseTransactionDTO(json: JSONObject): TransactionDTO {
        return TransactionDTO(
            transactionId = json.optString("transactionId"),
            bookId = json.optString("bookId"),
            bookTitle = json.optString("bookTitle"),
            userId = json.optString("userId"),
            renterUsername = json.optString("renterUsername"),
            ownerId = json.optString("ownerId"),
            ownerUsername = json.optString("ownerUsername"),
            startDate = json.optString("startDate"),
            endDate = json.optString("endDate"),
            amount = json.optDouble("amount").let { if (it.isNaN()) null else it },
            status = json.optString("status"),
            paymentMethod = json.optString("paymentMethod"),
            paymentStatus = json.optString("paymentStatus"),
            paymentDate = json.optString("paymentDate"),
            userRole = json.optString("userRole"),
            createdAt = json.optString("createdAt"),
            updatedAt = json.optString("updatedAt"),
            ownerRated = json.optBoolean("ownerRated"),
            renterRated = json.optBoolean("renterRated")
        )
    }

    private fun parseNotificationDTO(json: JSONObject): NotificationDTO {
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
