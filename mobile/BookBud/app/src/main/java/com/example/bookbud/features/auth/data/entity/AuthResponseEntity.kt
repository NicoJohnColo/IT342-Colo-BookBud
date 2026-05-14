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
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val googleId: String?,
    val rating: Double,
    val createdAt: String,
    val updatedAt: String
)
