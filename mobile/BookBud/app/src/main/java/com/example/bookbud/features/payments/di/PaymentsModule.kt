package com.example.bookbud.features.payments.di

import android.content.Context
import com.example.bookbud.shared.network.PaymentsApi
import com.example.bookbud.features.payments.data.remote.StripePaymentService
import com.example.bookbud.features.payments.domain.repository.PaymentsRepository
import com.example.bookbud.shared.network.StripeConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentsModule {
    
    @Provides
    @Singleton
    fun providePaymentsApi(retrofit: Retrofit): PaymentsApi {
        return retrofit.create(PaymentsApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideStripePaymentService(
        @ApplicationContext context: Context
    ): StripePaymentService {
        return StripePaymentService(context, StripeConfig.PUBLISHABLE_KEY)
    }
    
    @Provides
    @Singleton
    fun providePaymentsRepository(api: PaymentsApi): PaymentsRepository {
        return object : PaymentsRepository {
            override suspend fun getPayments(): List<String> = emptyList()
            override suspend fun processPayment(data: Map<String, String>): String = ""
        }
    }
}
