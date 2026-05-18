package com.example.bookbud.features.wishlist.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.WishlistApiClient
import kotlinx.coroutines.*

class WishlistFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWishlist(view)
    }

    private fun loadWishlist(view: View) {
        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: return@launch
            val wishlist = WishlistApiClient.getMyWishlist(token)
            
            withContext(Dispatchers.Main) {
                val container = view.findViewById<LinearLayout>(R.id.wishlistContainer)
                container?.removeAllViews()
                
                wishlist.forEach { item ->
                    val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_wishlist, container, false)
                    itemView.findViewById<TextView>(R.id.wishlistTitle)?.text = item.book?.title ?: "Book"
                    itemView.findViewById<TextView>(R.id.wishlistAuthor)?.text = item.book?.author ?: "Author"
                    container?.addView(itemView)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
