package com.example.bookbud.features.wishlist.domain.repository

interface WishlistRepository {
    suspend fun getWishlist(): List<String>
    suspend fun addToWishlist(bookId: String): String
    suspend fun removeFromWishlist(wishlistId: String): String
}
