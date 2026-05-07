package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object BookApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/books"

    fun getAllBooks(params: Map<String, String> = emptyMap()): ApiResponse<PaginatedResponse<BookDTO>> {
        return try {
            val urlBuilder = StringBuilder(BASE_URL)
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }

            val response = get(urlBuilder.toString())
            parseResponse<PaginatedResponse<BookDTO>>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun getMyBooks(accessToken: String, params: Map<String, String> = emptyMap()): ApiResponse<PaginatedResponse<BookDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/my")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }

            val response = getWithAuth(urlBuilder.toString(), accessToken)
            parseResponse<PaginatedResponse<BookDTO>>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun getBookById(bookId: String): ApiResponse<BookDTO> {
        return try {
            val response = get("$BASE_URL/$bookId")
            parseResponse<BookDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun createBook(accessToken: String, bookData: CreateBookRequest): ApiResponse<BookDTO> {
        return try {
            val body = JSONObject().apply {
                put("title", bookData.title)
                put("author", bookData.author)
                put("genre", bookData.genre)
                put("description", bookData.description)
                put("condition", bookData.condition)
                put("transactionType", bookData.transactionType)
                if (bookData.priceRent != null) put("priceRent", bookData.priceRent)
                if (bookData.priceSale != null) put("priceSale", bookData.priceSale)
            }
            val response = post(BASE_URL, body, accessToken)
            parseResponse<BookDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updateBook(accessToken: String, bookId: String, bookData: UpdateBookRequest): ApiResponse<BookDTO> {
        return try {
            val body = JSONObject().apply {
                if (bookData.title != null) put("title", bookData.title)
                if (bookData.author != null) put("author", bookData.author)
                if (bookData.genre != null) put("genre", bookData.genre)
                if (bookData.description != null) put("description", bookData.description)
                if (bookData.condition != null) put("condition", bookData.condition)
                if (bookData.transactionType != null) put("transactionType", bookData.transactionType)
                if (bookData.priceRent != null) put("priceRent", bookData.priceRent)
                if (bookData.priceSale != null) put("priceSale", bookData.priceSale)
                if (bookData.status != null) put("status", bookData.status)
            }
            val response = put("$BASE_URL/$bookId", body, accessToken)
            parseResponse<BookDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun deleteBook(accessToken: String, bookId: String): ApiResponse<String> {
        return try {
            val response = delete("$BASE_URL/$bookId", accessToken)
            parseResponse<String>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun uploadBookImage(accessToken: String, bookId: String, imageBytes: ByteArray): ApiResponse<BookDTO> {
        return try {
            val url = "$BASE_URL/$bookId/image"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            
            val boundary = "----BookBudBoundary${System.currentTimeMillis()}"
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.doOutput = true

            conn.outputStream.use { output ->
                output.write("--$boundary\r\n".toByteArray())
                output.write("Content-Disposition: form-data; name=\"image\"; filename=\"book.jpg\"\r\n".toByteArray())
                output.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
                output.write(imageBytes)
                output.write("\r\n--$boundary--\r\n".toByteArray())
                output.flush()
            }

            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = BufferedReader(InputStreamReader(stream)).readText()
            parseResponse<BookDTO>(response)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doInput = true

        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }

    private fun getWithAuth(url: String, accessToken: String): String {
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
            val data = root.optJSONObject("data")
            val message = root.optString("message")

            if (success && data != null) {
                // Parse data based on type
                val parsed: T? = when (T::class) {
                    BookDTO::class -> parseBookDTO(data) as? T
                    PaginatedResponse::class -> parsePaginatedBooks(data) as? T
                    String::class -> "Success" as? T
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

    private fun parseBookDTO(json: JSONObject): BookDTO {
        return BookDTO(
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
    }

    private fun parsePaginatedBooks(json: JSONObject): PaginatedResponse<BookDTO> {
        val content = mutableListOf<BookDTO>()
        val contentArray = json.optJSONArray("content")
        if (contentArray != null) {
            for (i in 0 until contentArray.length()) {
                content.add(parseBookDTO(contentArray.getJSONObject(i)))
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
