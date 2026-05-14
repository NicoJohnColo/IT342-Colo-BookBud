package com.example.bookbud.features.auth.data.remote

import com.example.bookbud.features.auth.data.entity.AuthResponseEntity
import com.example.bookbud.features.auth.domain.model.LoginRequest
import com.example.bookbud.features.auth.domain.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseEntity
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseEntity
    
    @POST("auth/google")
    suspend fun googleLogin(@Body idToken: Map<String, String>): AuthResponseEntity
    
    @POST("auth/logout")
    suspend fun logout()
    
    @POST("auth/refresh")
    suspend fun refreshToken(): Map<String, String>
}
