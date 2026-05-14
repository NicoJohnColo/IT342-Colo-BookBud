package com.example.bookbud.features.users.domain.repository

interface UsersRepository {
    suspend fun getUserProfile(userId: String): String
    suspend fun updateProfile(userId: String, data: Map<String, String>): String
}
