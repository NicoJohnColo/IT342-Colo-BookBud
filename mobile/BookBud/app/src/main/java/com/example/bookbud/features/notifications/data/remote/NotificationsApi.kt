package com.example.bookbud.features.notifications.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationsApi {
    @GET("notifications")
    suspend fun getNotifications(): String
    
    @POST("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: String): String
}
