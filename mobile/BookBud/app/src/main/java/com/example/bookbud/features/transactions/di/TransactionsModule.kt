package com.example.bookbud.features.transactions.di

import com.example.bookbud.features.transactions.data.remote.TransactionsApi
import com.example.bookbud.features.transactions.domain.repository.TransactionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TransactionsModule {
    
    @Provides
    @Singleton
    fun provideTransactionsApi(retrofit: Retrofit): TransactionsApi {
        return retrofit.create(TransactionsApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTransactionsRepository(api: TransactionsApi): TransactionsRepository {
        return object : TransactionsRepository {
            override suspend fun getTransactions(): List<String> = emptyList()
            override suspend fun getTransactionDetail(transactionId: String): String = ""
            override suspend fun createTransaction(data: Map<String, String>): String = ""
        }
    }
}
