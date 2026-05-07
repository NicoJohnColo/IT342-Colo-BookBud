package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class PaymentDTO(
    val paymentId: String? = null,
    val transactionId: String? = null,
    val amount: Double? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val paymentDate: String? = null,
    val createdAt: String? = null,
    val bookTitle: String? = null,
    val otherPartyName: String? = null
)

data class EarningsSummaryDTO(
    val totalEarnings: Double? = null,
    val pendingPayments: Int? = null,
    val successfulPayments: Int? = null,
    val failedPayments: Int? = null
)

object PaymentApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"

    fun getEarningsSummary(accessToken: String): ApiResponse<EarningsSummaryDTO> {
        return try {
            val response = getWithAuth("$BASE_URL/earnings/summary", accessToken)
            val root = JSONObject(response)
            val success = root.optBoolean("success", false)
            val data = root.optJSONObject("data")

            if (success && data != null) {
                val summary = EarningsSummaryDTO(
                    totalEarnings = data.optDouble("totalEarnings"),
                    pendingPayments = data.optInt("pendingPayments"),
                    successfulPayments = data.optInt("successfulPayments"),
                    failedPayments = data.optInt("failedPayments")
                )
                ApiResponse(success = true, data = summary)
            } else {
                ApiResponse(success = false, message = root.optString("message", "Failed to load earnings"))
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun getPaymentsReceived(accessToken: String, params: Map<String, String> = emptyMap()): ApiResponse<PaginatedResponse<PaymentDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/payments/received")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }

            val response = getWithAuth(urlBuilder.toString(), accessToken)
            ApiResponse(success = true, data = parsePaymentsResponse(response))
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun getMyPayments(accessToken: String, params: Map<String, String> = emptyMap()): ApiResponse<PaginatedResponse<PaymentDTO>> {
        return try {
            val urlBuilder = StringBuilder("$BASE_URL/payments")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                params.forEach { (key, value) ->
                    urlBuilder.append("$key=$value&")
                }
                urlBuilder.deleteCharAt(urlBuilder.length - 1)
            }

            val response = getWithAuth(urlBuilder.toString(), accessToken)
            ApiResponse(success = true, data = parsePaymentsResponse(response))
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    fun updatePaymentStatus(accessToken: String, paymentId: String, status: String): ApiResponse<String> {
        return try {
            val body = JSONObject().apply {
                put("status", status)
            }
            val response = putWithAuth("$BASE_URL/payments/$paymentId/status", body, accessToken)
            val root = JSONObject(response)
            val success = root.optBoolean("success", false)
            ApiResponse(success = success, data = if (success) "Updated" else null, message = root.optString("message"))
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message)
        }
    }

    private fun parsePaymentsResponse(json: String): PaginatedResponse<PaymentDTO> {
        val root = JSONObject(json)
        val data = root.optJSONObject("data") ?: root
        val contentArray = data.optJSONArray("content") ?: data.optJSONArray("data")

        val payments = mutableListOf<PaymentDTO>()
        if (contentArray != null) {
            for (i in 0 until contentArray.length()) {
                val item = contentArray.getJSONObject(i)
                payments.add(parsePayment(item))
            }
        }

        return PaginatedResponse(
            content = payments,
            page = data.optInt("page", 0),
            size = data.optInt("size", payments.size),
            totalElements = data.optLong("totalElements", payments.size.toLong())
        )
    }

    private fun parsePayment(json: JSONObject): PaymentDTO {
        return PaymentDTO(
            paymentId = json.optString("paymentId"),
            transactionId = json.optString("transactionId"),
            amount = json.optDouble("amount").takeIf { !it.isNaN() },
            paymentMethod = json.optString("paymentMethod"),
            paymentStatus = json.optString("paymentStatus"),
            paymentDate = json.optString("paymentDate"),
            createdAt = json.optString("createdAt"),
            bookTitle = json.optString("bookTitle"),
            otherPartyName = json.optString("otherPartyName")
        )
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

    private fun putWithAuth(url: String, body: JSONObject, accessToken: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.doOutput = true

        conn.outputStream.use { it.write(body.toString().toByteArray()) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }
}
