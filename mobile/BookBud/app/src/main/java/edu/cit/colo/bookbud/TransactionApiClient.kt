package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TransactionApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/transactions"

    fun createTransaction(
        accessToken: String,
        bookId: String,
        startDate: String? = null,
        endDate: String? = null
    ): ApiResponse<TransactionDTO> {
        return try {
            val body = JSONObject().apply {
                put("bookId", bookId)
                if (startDate != null) put("startDate", startDate)
                if (endDate != null) put("endDate", endDate)
            }
            val response = post(BASE_URL, body, accessToken)
            parseResponse<TransactionDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun getMyTransactions(
        accessToken: String,
        params: Map<String, String> = emptyMap()
    ): ApiResponse<PaginatedResponse<TransactionDTO>> {
        return try {
            val urlBuilder = StringBuilder(BASE_URL)
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }
            val response = get(urlBuilder.toString(), accessToken)
            parseResponse<PaginatedResponse<TransactionDTO>>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updateTransactionStatus(
        accessToken: String,
        transactionId: String,
        status: String
    ): ApiResponse<TransactionDTO> {
        return try {
            val body = JSONObject().apply {
                put("status", status)
            }
            val response = put("$BASE_URL/$transactionId/status", body, accessToken)
            parseResponse<TransactionDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun submitRating(
        accessToken: String,
        transactionId: String,
        rating: Double
    ): ApiResponse<RatingResponse> {
        return try {
            val body = JSONObject().apply {
                put("rating", rating)
            }
            val response = post("$BASE_URL/$transactionId/rating", body, accessToken)
            parseResponse<RatingResponse>(response)
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

    private inline fun <reified T> parseResponse(json: String): ApiResponse<T> {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success) {
                val parsed: T? = when (T::class) {
                    TransactionDTO::class -> {
                        if (data != null) parseTransactionDTO(data) as? T
                        else null
                    }
                    PaginatedResponse::class -> {
                        if (data != null) parsePaginatedTransactions(data) as? T
                        else null
                    }
                    RatingResponse::class -> {
                        if (data != null) parseRatingResponse(data) as? T
                        else null
                    }
                    else -> null
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
            amount = json.optDouble("amount"),
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

    private fun parseRatingResponse(json: JSONObject): RatingResponse {
        return RatingResponse(
            transactionId = json.optString("transactionId"),
            ratedUserId = json.optString("ratedUserId"),
            rating = json.optDouble("rating"),
            newAggregateRating = json.optDouble("newAggregateRating")
        )
    }

    private fun parsePaginatedTransactions(json: JSONObject): PaginatedResponse<TransactionDTO> {
        val content = mutableListOf<TransactionDTO>()
        val contentArray = json.optJSONArray("content")
        if (contentArray != null) {
            for (i in 0 until contentArray.length()) {
                content.add(parseTransactionDTO(contentArray.getJSONObject(i)))
            }
        }
        return PaginatedResponse(
            content = content,
            page = json.optInt("page"),
            size = json.optInt("size"),
            totalElements = json.optLong("totalElements")
        )
    }
}
