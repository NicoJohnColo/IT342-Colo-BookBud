package com.example.bookbud.features.users.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsersApi {
    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): String
    
    @PUT("users/{id}")
    suspend fun updateProfile(@Path("id") userId: String, @Body data: Map<String, String>): String
}
