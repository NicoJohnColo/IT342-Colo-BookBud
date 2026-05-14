package com.example.bookbud.features.auth.domain.model

data class User(
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val googleId: String?,
    val rating: Double,
    val createdAt: String,
    val updatedAt: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
