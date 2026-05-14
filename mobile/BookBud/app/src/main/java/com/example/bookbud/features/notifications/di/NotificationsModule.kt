package com.example.bookbud.features.notifications.di

import com.example.bookbud.features.notifications.data.remote.NotificationsApi
import com.example.bookbud.features.notifications.domain.repository.NotificationsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {
    
    @Provides
    @Singleton
    fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi {
        return retrofit.create(NotificationsApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNotificationsRepository(api: NotificationsApi): NotificationsRepository {
        return object : NotificationsRepository {
            override suspend fun getNotifications(): List<String> = emptyList()
            override suspend fun markAsRead(notificationId: String): String = ""
        }
    }
}
