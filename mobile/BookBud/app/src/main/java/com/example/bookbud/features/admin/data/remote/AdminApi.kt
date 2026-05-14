package com.example.bookbud.features.admin.data.remote

import retrofit2.http.GET

interface AdminApi {
    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): String
    
    @GET("admin/users")
    suspend fun getUsers(): String
    
    @GET("admin/books")
    suspend fun getBooks(): String
}
