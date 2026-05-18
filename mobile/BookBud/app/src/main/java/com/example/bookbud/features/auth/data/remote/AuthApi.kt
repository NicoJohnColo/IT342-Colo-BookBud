package com.example.bookbud.features.auth.data.remote

import com.example.bookbud.features.auth.data.entity.AuthResponseEntity
import com.example.bookbud.features.auth.domain.model.LoginRequest
import com.example.bookbud.features.auth.domain.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseEntity
    
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseEntity
    
    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body idToken: Map<String, String>): AuthResponseEntity
    
    @POST("api/v1/auth/logout")
    suspend fun logout()
    
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(): Map<String, String>
}
