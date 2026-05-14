package com.example.bookbud.features.notifications.domain.repository

interface NotificationsRepository {
    suspend fun getNotifications(): List<String>
    suspend fun markAsRead(notificationId: String): String
}
