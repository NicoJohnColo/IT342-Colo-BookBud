package com.example.bookbud.features.auth.data.entity

import com.google.gson.annotations.SerializedName

data class AuthResponseEntity(
    val data: AuthDataEntity
)

data class AuthDataEntity(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    val user: UserEntity
)

data class UserEntity(
    val userId: String?,
    val email: String?,
    val username: String?,
    val role: String?,
    val rating: String?,
    val createdAt: String?
)
