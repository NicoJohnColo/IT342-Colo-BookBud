package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(
        val message: String,
        val username: String?,
        val accessToken: String?,
        val refreshToken: String?,
        val userId: String? = null,
        val role: String? = null
    ) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthApiClient {

    // Try multiple backend URLs for connectivity
    private val BASE_URLS = listOf(
        "http://10.0.2.2:8080/api/v1/auth",  // Android emulator
        "http://10.0.2.2:8080/api/v1/auth",  // Android emulator (backup)
        "http://localhost:8080/api/v1/auth"   // Physical device
    )
    
    private fun getWorkingBaseUrl(): String {
        return BASE_URLS.firstOrNull { url ->
            try {
                val testUrl = URL(url.replace("/auth", ""))
                val conn = testUrl.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val responseCode = conn.responseCode
                responseCode in 200..499
            } catch (e: Exception) {
                false
            }
        } ?: BASE_URLS[0] // Fallback to first URL
    }

    fun register(name: String, email: String, password: String, confirmPassword: String): AuthResult {
        val body = JSONObject().apply {
            put("username", name)
            put("email", email)
            put("password", password)
            put("confirmPassword", confirmPassword)
        }
        return post("/register", body)
    }

    fun login(email: String, password: String): AuthResult {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        return post("/login", body)
    }

    fun googleAuth(idToken: String): AuthResult {
        val body = JSONObject().apply {
            put("idToken", idToken)
        }
        return post("/google", body)
    }

    private fun post(path: String, body: JSONObject): AuthResult {
        return try {
            val baseUrl = getWorkingBaseUrl()
            val url = URL(baseUrl + path)
            println("Auth API: Connecting to ${url}")
            
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000 // 10 seconds
            conn.readTimeout = 10000 // 10 seconds

            println("Auth API: Sending body: ${body}")

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(body.toString())
            }

            val responseCode = conn.responseCode
            println("Auth API: Response code: $responseCode")
            
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream

            val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }

            println("Auth API: Response: $responseText")
            parseAuthResponse(responseText)
        } catch (e: Exception) {
            println("Auth API Exception: ${e.message}")
            e.printStackTrace()
            AuthResult.Error("Could not connect to server: ${e.message}")
        }
    }

    private fun parseAuthResponse(json: String): AuthResult {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            if (success) {
                val data = root.optJSONObject("data")
                val user = data?.optJSONObject("user")
                val userId = user?.optString("userId")
                val username = user?.optString("username")
                val role = user?.optString("role") ?: data?.optString("role")
                val accessToken = data?.optString("accessToken")
                val refreshToken = data?.optString("refreshToken")
                AuthResult.Success("Success", username, accessToken, refreshToken, userId, role)
            } else {
                val error = root.optJSONObject("error")
                val message = error?.optString("message") ?: "Request failed. Please try again."
                // Log the full response for debugging
                println("Auth Error Response: $json")
                AuthResult.Error(message)
            }
        } catch (e: Exception) {
            println("Auth Parse Error: ${e.message}, Response: $json")
            AuthResult.Error("Unexpected response from server: ${e.message}")
        }
    }
}
