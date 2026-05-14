package com.example.bookbud.features.payments.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentsApi {
    @GET("payments")
    suspend fun getPayments(): String
    
    @POST("payments/process")
    suspend fun processPayment(@Body data: Map<String, String>): String
}
