package com.example.bookbud.features.auth.domain.repository

import com.example.bookbud.features.auth.domain.model.AuthResponse
import com.example.bookbud.features.auth.domain.model.LoginRequest
import com.example.bookbud.features.auth.domain.model.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun googleLogin(idToken: String): AuthResponse
    suspend fun logout()
    suspend fun refreshToken(): String
}
