package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminBookFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var booksRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchBooks: EditText
    private lateinit var resultsCount: TextView
    private var accessToken: String? = null
    private lateinit var bookAdapter: AdminBookAdapter

    // Filter views
    private lateinit var filterAll: TextView
    private lateinit var filterAvailable: TextView
    private lateinit var filterRented: TextView
    private lateinit var filterOnHold: TextView
    private lateinit var filterSold: TextView

    private var selectedFilter = "all"
    private var allBooks: List<BookDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBooks)
        booksRecycler = view.findViewById(R.id.booksRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        searchBooks = view.findViewById(R.id.searchBooks)
        resultsCount = view.findViewById(R.id.resultsCount)

        // Filter chips
        filterAll = view.findViewById(R.id.filterAll)
        filterAvailable = view.findViewById(R.id.filterAvailable)
        filterRented = view.findViewById(R.id.filterRented)
        filterOnHold = view.findViewById(R.id.filterOnHold)
        filterSold = view.findViewById(R.id.filterSold)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        bookAdapter = AdminBookAdapter(
            onSetUnavailable = { book ->
                book.bookId?.let { updateBookStatus(it, "Unavailable") }
            },
            onDelete = { book ->
                showDeleteConfirmation(book)
            }
        )
        booksRecycler.layoutManager = LinearLayoutManager(requireContext())
        booksRecycler.adapter = bookAdapter

        setupFilters()
        loadBooks()
    }

    private fun setupFilters() {
        filterAll.setOnClickListener { setFilter("all", filterAll, listOf(filterAvailable, filterRented, filterOnHold, filterSold)) }
        filterAvailable.setOnClickListener { setFilter("available", filterAvailable, listOf(filterAll, filterRented, filterOnHold, filterSold)) }
        filterRented.setOnClickListener { setFilter("rented", filterRented, listOf(filterAll, filterAvailable, filterOnHold, filterSold)) }
        filterOnHold.setOnClickListener { setFilter("on_hold", filterOnHold, listOf(filterAll, filterAvailable, filterRented, filterSold)) }
        filterSold.setOnClickListener { setFilter("sold", filterSold, listOf(filterAll, filterAvailable, filterRented, filterOnHold)) }

        // Search listener
        searchBooks.setOnEditorActionListener { _, _, _ ->
            applyFilters()
            true
        }
    }

    private fun setFilter(status: String, selected: TextView, others: List<TextView>) {
        selectedFilter = status
        selected.setBackgroundResource(R.drawable.bg_badge_hot)
        selected.setTextColor(resources.getColor(android.R.color.white, null))
        others.forEach {
            it.setBackgroundResource(R.drawable.bg_badge_gray)
            it.setTextColor(resources.getColor(R.color.bb_text_dark, null))
        }
        applyFilters()
    }

    private fun applyFilters() {
        val query = searchBooks.text.toString().lowercase()

        val filtered = allBooks.filter { book ->
            val matchesSearch = query.isEmpty() ||
                    book.title?.lowercase()?.contains(query) == true ||
                    book.author?.lowercase()?.contains(query) == true

            val matchesStatus = when (selectedFilter) {
                "available" -> book.status?.lowercase() == "available"
                "rented" -> book.status?.lowercase() == "rented"
                "on_hold" -> book.status?.lowercase() == "on_hold"
                "sold" -> book.status?.lowercase() == "sold"
                else -> true
            }

            matchesSearch && matchesStatus
        }

        bookAdapter.updateData(filtered)
        resultsCount.text = "${filtered.size} results"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        booksRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadBooks() {
        progressBar.visibility = View.VISIBLE
        booksRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.getAllBooks(accessToken, mapOf("size" to "100"))
                allBooks = (result.data as? PaginatedResponse<BookDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    resultsCount.text = "${allBooks.size} results"

                    if (allBooks.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        booksRecycler.visibility = View.VISIBLE
                        applyFilters()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateBookStatus(bookId: String, status: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.updateBookStatus(accessToken, bookId, status)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Book status updated to $status", Toast.LENGTH_SHORT).show()
                        loadBooks()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showDeleteConfirmation(book: BookDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete \"${book.title}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBook(book: BookDTO) {
        val bookId = book.bookId ?: return

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.deleteBook(accessToken, bookId)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Book deleted successfully!", Toast.LENGTH_SHORT).show()
                        loadBooks()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to delete book", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}

// Admin Book Adapter
class AdminBookAdapter(
    private var books: List<BookDTO> = emptyList(),
    private val onSetUnavailable: (BookDTO) -> Unit,
    private val onDelete: (BookDTO) -> Unit
) : RecyclerView.Adapter<AdminBookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView = itemView.findViewById<TextView>(R.id.bookAvatar)
        private val titleView = itemView.findViewById<TextView>(R.id.bookTitle)
        private val authorView = itemView.findViewById<TextView>(R.id.bookAuthor)
        private val genreView = itemView.findViewById<TextView>(R.id.bookGenre)
        private val priceView = itemView.findViewById<TextView>(R.id.bookPrice)
        private val statusView = itemView.findViewById<TextView>(R.id.bookTransactionType)
        private val ownerView = itemView.findViewById<TextView>(R.id.bookWishlistIcon)
        private val btnUnavailable = itemView.findViewById<Button>(R.id.btnUnavailable)
        private val btnDelete = itemView.findViewById<Button>(R.id.btnDelete)

        fun bind(book: BookDTO) {
            val initials = book.title?.firstOrNull()?.uppercase() ?: "B"
            avatarView.text = initials

            titleView.text = book.title ?: "Unknown"
            authorView.text = book.author ?: "Unknown"
            genreView.text = "${book.genre} • ${book.condition}"
            priceView.text = "₱${(book.priceSale ?: book.priceRent ?: 0.0).toInt()}"
            statusView.text = book.status ?: "Unknown"
            statusView.background = when (book.status?.lowercase()) {
                "available" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_badge_hot)
                "rented" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_active)
                "sold" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_completed)
                else -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_badge_gray)
            }
            ownerView.text = "@${book.ownerUsername ?: book.ownerId ?: "unknown"}"

            btnUnavailable?.setOnClickListener { onSetUnavailable(book) }
            btnDelete?.setOnClickListener { onDelete(book) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<BookDTO>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
