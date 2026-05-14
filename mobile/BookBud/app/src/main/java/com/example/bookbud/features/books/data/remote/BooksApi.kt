package com.example.bookbud.features.books.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BooksApi {
    @GET("books")
    suspend fun getBooks(@Query("genre") genre: String?, @Query("condition") condition: String?): String
    
    @GET("books/{id}")
    suspend fun getBookDetail(@Path("id") bookId: String): String
}
