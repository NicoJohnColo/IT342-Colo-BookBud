package com.example.bookbud.features.payments.domain.repository

interface PaymentsRepository {
    suspend fun getPayments(): List<String>
    suspend fun processPayment(data: Map<String, String>): String
}
