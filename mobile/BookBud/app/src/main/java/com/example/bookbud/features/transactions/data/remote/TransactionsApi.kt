package com.example.bookbud.features.transactions.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TransactionsApi {
    @GET("transactions")
    suspend fun getTransactions(): String
    
    @GET("transactions/{id}")
    suspend fun getTransactionDetail(@Path("id") transactionId: String): String
    
    @POST("transactions")
    suspend fun createTransaction(@Body data: Map<String, String>): String
}
