package com.example.bookbud.features.auth.di

import com.example.bookbud.features.auth.data.remote.AuthApi
import com.example.bookbud.features.auth.data.repository.AuthRepositoryImpl
import com.example.bookbud.features.auth.domain.repository.AuthRepository
import com.example.bookbud.features.auth.domain.usecase.LoginUseCase
import com.example.bookbud.features.auth.domain.usecase.RegisterUseCase
import com.example.bookbud.shared.storage.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, preferencesManager)
    }
    
    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }
    
    @Provides
    @Singleton
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }
}
