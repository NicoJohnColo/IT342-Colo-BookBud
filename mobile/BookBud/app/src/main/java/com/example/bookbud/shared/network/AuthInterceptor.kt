package com.example.bookbud.shared.network

import com.example.bookbud.shared.storage.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferencesManager: PreferencesManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val token = preferencesManager.getToken()
        val requestBuilder = originalRequest.newBuilder()
        
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(requestBuilder.build())
        
        // Handle 401 - token expired
        if (response.code == 401) {
            // Attempt to refresh token or logout
            preferencesManager.clearAll()
        }
        
        return response
    }
}
