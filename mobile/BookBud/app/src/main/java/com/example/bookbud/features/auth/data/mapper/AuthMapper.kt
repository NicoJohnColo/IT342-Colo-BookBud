package com.example.bookbud.features.auth.data.mapper

import com.example.bookbud.features.auth.data.entity.UserEntity
import com.example.bookbud.features.auth.domain.model.User

fun UserEntity.toDomain() = User(
    userId = userId,
    email = email,
    firstName = firstName,
    lastName = lastName,
    googleId = googleId,
    rating = rating,
    createdAt = createdAt,
    updatedAt = updatedAt
)
