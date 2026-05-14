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
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun register(request: RegisterRequest): AuthResponse {
        val response = authApi.register(request)
        preferencesManager.saveToken(response.data.accessToken)
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun googleLogin(idToken: String): AuthResponse {
        val response = authApi.googleLogin(mapOf("idToken" to idToken))
        preferencesManager.saveToken(response.data.accessToken)
        return AuthResponse(
            accessToken = response.data.accessToken,
            refreshToken = response.data.refreshToken,
            user = response.data.user.toDomain()
        )
    }
    
    override suspend fun logout() {
        authApi.logout()
        preferencesManager.clearAll()
    }
    
    override suspend fun refreshToken(): String {
        val response = authApi.refreshToken()
        val token = response["accessToken"] ?: throw Exception("Token refresh failed")
        preferencesManager.saveToken(token)
        return token
    }
}
