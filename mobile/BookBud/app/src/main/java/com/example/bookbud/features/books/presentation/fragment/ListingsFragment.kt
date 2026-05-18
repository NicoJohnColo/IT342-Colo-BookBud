package com.example.bookbud.features.books.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.BookApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class ListingsFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddListing)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreateListingFragment())
                .addToBackStack(null)
                .commit()
        }

        loadListings(view)
    }

    private fun loadListings(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressListings)
        val container = view.findViewById<LinearLayout>(R.id.listingsContainer)
        val emptyState = view.findViewById<TextView>(R.id.emptyState)
        val countText = view.findViewById<TextView>(R.id.textListingsCount)

        progressBar?.visibility = View.VISIBLE
        emptyState?.visibility = View.GONE
        container?.removeAllViews()

        job = lifecycleScope.launch {
            val userId = TokenManager.getUserId() ?: ""
            val books = BookApiClient.getAllBooks()
            
            withContext(Dispatchers.Main) {
                progressBar?.visibility = View.GONE
                val myListings = books?.filter { it.ownerId == userId } ?: emptyList()
                
                countText?.text = "${myListings.size} books listed"

                if (myListings.isEmpty()) {
                    emptyState?.visibility = View.VISIBLE
                } else {
                    myListings.forEach { book ->
                        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_book, container, false)
                        itemView.findViewById<TextView>(R.id.bookTitle)?.text = book.title
                        itemView.findViewById<TextView>(R.id.bookAuthor)?.text = book.author
                        itemView.findViewById<TextView>(R.id.bookPrice)?.text = String.format("PHP %.2f", book.priceSale ?: book.priceRent ?: 0.0)
                        container?.addView(itemView)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
