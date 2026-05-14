package com.example.bookbud.features.admin.di

import com.example.bookbud.features.admin.data.remote.AdminApi
import com.example.bookbud.features.admin.domain.repository.AdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {
    
    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi {
        return retrofit.create(AdminApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAdminRepository(api: AdminApi): AdminRepository {
        return object : AdminRepository {
            override suspend fun getAdminDashboard(): String = ""
            override suspend fun getUsers(): List<String> = emptyList()
            override suspend fun getBooks(): List<String> = emptyList()
        }
    }
}
