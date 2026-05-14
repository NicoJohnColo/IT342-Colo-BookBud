package com.example.bookbud.features.books.di

import com.example.bookbud.features.books.data.remote.BooksApi
import com.example.bookbud.features.books.domain.repository.BooksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BooksModule {
    
    @Provides
    @Singleton
    fun provideBooksApi(retrofit: Retrofit): BooksApi {
        return retrofit.create(BooksApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideBooksRepository(booksApi: BooksApi): BooksRepository {
        return object : BooksRepository {
            override suspend fun getBooks(genre: String?, condition: String?): List<String> {
                return emptyList()
            }
            override suspend fun getBookDetail(bookId: String): String = ""
        }
    }
}
