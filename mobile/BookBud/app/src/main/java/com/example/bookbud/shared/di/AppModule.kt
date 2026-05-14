package com.example.bookbud.shared.di

import android.content.Context
import com.example.bookbud.shared.network.RetrofitClient
import com.example.bookbud.shared.storage.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        PreferencesManager.init(context)
        return PreferencesManager
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(preferencesManager: PreferencesManager): Retrofit {
        return RetrofitClient.create(preferencesManager)
    }
}
