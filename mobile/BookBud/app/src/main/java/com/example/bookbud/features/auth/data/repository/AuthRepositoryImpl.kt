package com.example.bookbud.features.auth.data.repository

import com.example.bookbud.features.auth.data.mapper.toDomain
import com.example.bookbud.features.auth.data.remote.AuthApi
import com.example.bookbud.features.auth.domain.model.AuthResponse
import com.example.bookbud.features.auth.domain.model.LoginRequest
import com.example.bookbud.features.auth.domain.model.RegisterRequest
import com.example.bookbud.features.auth.domain.repository.AuthRepository
import com.example.bookbud.shared.storage.PreferencesManager

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): AuthResponse {
        val response = authApi.login(request)
        preferencesManager.saveToken(response.data.accessToken)
        
        // Save to TokenManager for UI and Shared Network layers
        com.example.bookbud.shared.auth.TokenManager.saveAuthResponse(
            com.example.bookbud.shared.models.AuthResponse(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken,
                userId = response.data.user.userId ?: "",
                username = response.data.user.username ?: response.data.user.email ?: "Reader",
                email = response.data.user.email ?: "",
                role = response.data.user.role ?: "USER"
            )
        )
        
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = authApi.register(request)
        preferencesManager.saveToken(response.data.accessToken)
        
        // Save to TokenManager for UI and Shared Network layers
        com.example.bookbud.shared.auth.TokenManager.saveAuthResponse(
            com.example.bookbud.shared.models.AuthResponse(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken,
                userId = response.data.user.userId ?: "",
                username = response.data.user.username ?: response.data.user.email ?: "Reader",
                email = response.data.user.email ?: "",
                role = response.data.user.role ?: "USER"
            )
        )
        
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun googleLogin(idToken: String): AuthResponse {
        val response = authApi.googleLogin(mapOf("idToken" to idToken))
        preferencesManager.saveToken(response.data.accessToken)
        
        // Save to TokenManager for UI and Shared Network layers
        com.example.bookbud.shared.auth.TokenManager.saveAuthResponse(
            com.example.bookbud.shared.models.AuthResponse(
                accessToken = response.data.accessToken,
                refreshToken = response.data.refreshToken,
                userId = response.data.user.userId ?: "",
                username = response.data.user.username ?: response.data.user.email ?: "Reader",
                email = response.data.user.email ?: "",
                role = response.data.user.role ?: "USER"
            )
        )
        
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun logout() {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Ignore API failures during logout so clearAll still works
        }
        preferencesManager.clearAll()
        com.example.bookbud.shared.auth.TokenManager.clearAll()
    }
    
    override suspend fun refreshToken(): String {
        val response = authApi.refreshToken()
        val token = response["accessToken"] ?: throw Exception("Token refresh failed")
        preferencesManager.saveToken(token)
        return token
    }
}
