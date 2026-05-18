package com.example.bookbud.features.auth.data.mapper

import com.example.bookbud.features.auth.data.entity.UserEntity
import com.example.bookbud.features.auth.domain.model.User

fun UserEntity.toDomain() = User(
    userId = userId ?: "",
    email = email ?: "",
    firstName = (username ?: email ?: "Reader").trim().split("\\s+".toRegex()).getOrNull(0) ?: "Reader",
    lastName = (username ?: "").trim().split("\\s+".toRegex()).let { parts ->
        if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""
    },
    googleId = null,
    rating = try { rating?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 },
    createdAt = createdAt ?: "",
    updatedAt = createdAt ?: ""
)
