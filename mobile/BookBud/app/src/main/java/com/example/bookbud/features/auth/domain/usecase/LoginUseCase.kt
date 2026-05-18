package com.example.bookbud.features.auth.domain.usecase

import com.example.bookbud.features.auth.domain.model.AuthResponse
import com.example.bookbud.features.auth.domain.model.LoginRequest
import com.example.bookbud.features.auth.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResponse {
        return authRepository.login(LoginRequest(email, password))
    }
    
    suspend fun googleLogin(idToken: String): AuthResponse {
        return authRepository.googleLogin(idToken)
    }
}
