package com.example.bookbud.features.users.di

import com.example.bookbud.features.users.data.remote.UsersApi
import com.example.bookbud.features.users.domain.repository.UsersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsersModule {
    
    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi {
        return retrofit.create(UsersApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUsersRepository(usersApi: UsersApi): UsersRepository {
        return object : UsersRepository {
            override suspend fun getUserProfile(userId: String): String = ""
            override suspend fun updateProfile(userId: String, data: Map<String, String>): String = ""
        }
    }
}
