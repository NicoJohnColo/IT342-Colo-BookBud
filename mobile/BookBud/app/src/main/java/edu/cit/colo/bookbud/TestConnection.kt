package edu.cit.colo.bookbud

import java.net.HttpURLConnection
import java.net.URL

object TestConnection {
    
    fun testBackendConnection(): Boolean {
        return try {
            val url = URL("http://10.0.2.2:8080/api/v1/auth/login")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            
            val responseCode = conn.responseCode
            println("Backend Test: Response code: $responseCode")
            
            responseCode in 200..499 // Any response means server is running
        } catch (e: Exception) {
            println("Backend Test: Connection failed - ${e.message}")
            false
        }
    }
    
    fun testGoogleAuthEndpoint(): Boolean {
        return try {
            val url = URL("http://10.0.2.2:8080/api/v1/auth/google")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            
            // Send empty body to test endpoint exists
            conn.outputStream.write("{\"idToken\":\"test\"}".toByteArray())
            
            val responseCode = conn.responseCode
            println("Google Auth Test: Response code: $responseCode")
            
            responseCode != 404 // Endpoint exists (even if auth fails)
        } catch (e: Exception) {
            println("Google Auth Test: Connection failed - ${e.message}")
            false
        }
    }
}
