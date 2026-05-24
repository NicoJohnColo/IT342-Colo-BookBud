package com.example.bookbud.features.admin.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbud.R
import com.example.bookbud.features.admin.presentation.adapter.AdminBookAdapter
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.BookDTO
import com.example.bookbud.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminBooksFragment : Fragment() {
    private lateinit var progressBooks: ProgressBar
    private lateinit var booksRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var resultsCount: TextView
    private lateinit var searchBooks: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterAvailable: TextView
    private lateinit var filterRented: TextView
    private lateinit var filterOnHold: TextView
    private lateinit var filterSold: TextView
    private lateinit var btnBack: ImageButton

    private val allBooks = mutableListOf<BookDTO>()
    private var activeFilter = "All"
    private var searchQuery = ""

    private val adapter = AdminBookAdapter(
        onViewDetails = { showBookDetails(it) },
        onSetUnavailable = { updateBookStatus(it, "Unavailable") },
        onDelete = { deleteBook(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBooks = view.findViewById(R.id.progressBooks)
        booksRecycler = view.findViewById(R.id.booksRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        resultsCount = view.findViewById(R.id.resultsCount)
        searchBooks = view.findViewById(R.id.searchBooks)
        filterAll = view.findViewById(R.id.filterAll)
        filterAvailable = view.findViewById(R.id.filterAvailable)
        filterRented = view.findViewById(R.id.filterRented)
        filterOnHold = view.findViewById(R.id.filterOnHold)
        filterSold = view.findViewById(R.id.filterSold)
        btnBack = view.findViewById(R.id.btnBack)

        booksRecycler.layoutManager = LinearLayoutManager(requireContext())
        booksRecycler.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        searchBooks.doAfterTextChanged {
            searchQuery = it?.toString().orEmpty()
            applyFilters()
        }

        filterAll.setOnClickListener { activeFilter = "All"; applyFilters() }
        filterAvailable.setOnClickListener { activeFilter = "Available"; applyFilters() }
        filterRented.setOnClickListener { activeFilter = "Rented"; applyFilters() }
        filterOnHold.setOnClickListener { activeFilter = "Unavailable"; applyFilters() }
        filterSold.setOnClickListener { activeFilter = "Sold"; applyFilters() }

        loadBooks()
    }

    private fun loadBooks() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    showError("Not authenticated")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.getBooks("Bearer $token", size = 100)
                }

                withContext(Dispatchers.Main) {
                    progressBooks.visibility = View.GONE
                    allBooks.clear()
                    allBooks.addAll(response.data?.content.orEmpty())
                    applyFilters()
                }
            } catch (e: Exception) {
                showError("Failed to load books: ${e.message}")
            }
        }
    }

    private fun applyFilters() {
        val filtered = allBooks.filter { book ->
            val status = book.status?.takeIf { it.isNotBlank() } ?: "Unknown"
            val matchesFilter = activeFilter == "All" || status.equals(activeFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || listOf(book.title, book.author, book.ownerUsername, book.genre)
                .any { it?.contains(searchQuery, ignoreCase = true) == true }
            matchesFilter && matchesSearch
        }

        adapter.submitList(filtered)
        resultsCount.text = "${filtered.size} results"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        booksRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        emptyState.text = if (allBooks.isEmpty()) "No books found" else "No books match your filters"
    }

    private fun showBookDetails(book: BookDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle(book.title)
            .setMessage(
                buildString {
                    appendLine("Author: ${book.author}")
                    appendLine("Genre: ${book.genre ?: "N/A"}")
                    appendLine("Condition: ${book.condition ?: "N/A"}")
                    appendLine("Type: ${book.transactionType ?: "N/A"}")
                    appendLine("Status: ${book.status ?: "N/A"}")
                    appendLine("Owner: ${book.ownerUsername ?: book.ownerId}")
                    appendLine("Listed: ${book.createdAt ?: "N/A"}")
                    appendLine("Rent: ${book.priceRent ?: 0.0}")
                    appendLine("Sale: ${book.priceSale ?: 0.0}")
                }
            )
            .setPositiveButton("Set Unavailable") { _, _ -> updateBookStatus(book, "Unavailable") }
            .setNegativeButton("Delete") { _, _ -> deleteBook(book) }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun updateBookStatus(book: BookDTO, status: String) {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken() ?: return@launch
                withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.updateBookStatus(book.bookId, "Bearer $token", status)
                }
                Toast.makeText(requireContext(), "Book status updated", Toast.LENGTH_SHORT).show()
                loadBooks()
            } catch (e: Exception) {
                showError("Failed to update status: ${e.message}")
            }
        }
    }

    private fun deleteBook(book: BookDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Delete ${book.title} from the platform?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val token = TokenManager.getAccessToken() ?: return@launch
                        withContext(Dispatchers.IO) {
                            RetrofitClient.adminApi.deleteBook(book.bookId, "Bearer $token")
                        }
                        Toast.makeText(requireContext(), "Book deleted", Toast.LENGTH_SHORT).show()
                        loadBooks()
                    } catch (e: Exception) {
                        showError("Failed to delete book: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressBooks.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyState.text = message
            booksRecycler.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
