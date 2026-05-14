package com.example.bookbud.features.books.domain.repository

interface BooksRepository {
    suspend fun getBooks(genre: String? = null, condition: String? = null): List<String>
    suspend fun getBookDetail(bookId: String): String
}
