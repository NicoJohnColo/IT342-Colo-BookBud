package edu.cit.colo.bookbud

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val message: String, val username: String?, val accessToken: String?, val refreshToken: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthApiClient {

    // Use 10.0.2.2 to reach localhost from Android emulator
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/auth"

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

    private fun post(path: String, body: JSONObject): AuthResult {
        return try {
            val url = URL(BASE_URL + path)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(body.toString())
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream

            val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }

            parseAuthResponse(responseText)
        } catch (e: Exception) {
            AuthResult.Error("Could not connect to server. Please try again.")
        }
    }

    private fun parseAuthResponse(json: String): AuthResult {
        return try {
            val root = JSONObject(json)
            val success = root.optBoolean("success", false)
            if (success) {
                val data = root.optJSONObject("data")
                val user = data?.optJSONObject("user")
                val username = user?.optString("username")
                val accessToken = data?.optString("accessToken")
                val refreshToken = data?.optString("refreshToken")
                AuthResult.Success("Success", username, accessToken, refreshToken)
            } else {
                val error = root.optJSONObject("error")
                val message = error?.optString("message") ?: "Request failed. Please try again."
                AuthResult.Error(message)
            }
        } catch (e: Exception) {
            AuthResult.Error("Unexpected response from server.")
        }
    }
}
