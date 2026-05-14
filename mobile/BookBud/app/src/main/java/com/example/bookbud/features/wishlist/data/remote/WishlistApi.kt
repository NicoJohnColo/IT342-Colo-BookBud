package com.example.bookbud.features.wishlist.data.remote

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WishlistApi {
    @GET("wishlist")
    suspend fun getWishlist(): String
    
    @POST("wishlist/add/{bookId}")
    suspend fun addToWishlist(@Path("bookId") bookId: String): String
    
    @DELETE("wishlist/{id}")
    suspend fun removeFromWishlist(@Path("id") wishlistId: String): String
}
