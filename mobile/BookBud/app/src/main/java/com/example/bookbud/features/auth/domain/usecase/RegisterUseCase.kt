package com.example.bookbud.features.auth.domain.usecase

import com.example.bookbud.features.auth.domain.model.AuthResponse
import com.example.bookbud.features.auth.domain.model.RegisterRequest
import com.example.bookbud.features.auth.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(firstName: String, lastName: String, email: String, password: String): AuthResponse {
        return authRepository.register(RegisterRequest(firstName, lastName, email, password))
    }
}
