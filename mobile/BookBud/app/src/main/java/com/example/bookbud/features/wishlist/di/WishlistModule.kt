package com.example.bookbud.features.wishlist.di

import com.example.bookbud.features.wishlist.data.remote.WishlistApi
import com.example.bookbud.features.wishlist.domain.repository.WishlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WishlistModule {
    
    @Provides
    @Singleton
    fun provideWishlistApi(retrofit: Retrofit): WishlistApi {
        return retrofit.create(WishlistApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideWishlistRepository(api: WishlistApi): WishlistRepository {
        return object : WishlistRepository {
            override suspend fun getWishlist(): List<String> = emptyList()
            override suspend fun addToWishlist(bookId: String): String = ""
            override suspend fun removeFromWishlist(wishlistId: String): String = ""
        }
    }
}
