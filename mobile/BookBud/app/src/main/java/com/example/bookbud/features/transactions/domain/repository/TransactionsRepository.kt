package com.example.bookbud.features.transactions.domain.repository

interface TransactionsRepository {
    suspend fun getTransactions(): List<String>
    suspend fun getTransactionDetail(transactionId: String): String
    suspend fun createTransaction(data: Map<String, String>): String
}
