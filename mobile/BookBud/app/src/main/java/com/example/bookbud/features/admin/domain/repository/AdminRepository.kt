package com.example.bookbud.features.admin.domain.repository

interface AdminRepository {
    suspend fun getAdminDashboard(): String
    suspend fun getUsers(): List<String>
    suspend fun getBooks(): List<String>
}
