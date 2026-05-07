package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListingsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var listingsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var textMyListings: TextView
    private lateinit var textListingsCount: TextView
    private lateinit var fabAddListing: FloatingActionButton
    private var accessToken: String? = null
    private var userId: String? = null
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressListings)
        listingsRecycler = view.findViewById(R.id.listingsRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        textMyListings = view.findViewById(R.id.textMyListings)
        textListingsCount = view.findViewById(R.id.textListingsCount)
        fabAddListing = view.findViewById(R.id.fabAddListing)

        // Back button - navigate to Profile
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)

        bookAdapter = BookAdapter(
            onBookClick = { book ->
                // Open book detail
                Toast.makeText(requireContext(), "Book: ${book.title}", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { book ->
                // Open edit fragment
                book.bookId?.let { bookId ->
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, EditListingFragment.newInstance(bookId))
                        .addToBackStack(null)
                        .commit()
                }
            },
            onDeleteClick = { book ->
                // Show delete confirmation
                showDeleteConfirmation(book)
            }
        )
        listingsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        listingsRecycler.adapter = bookAdapter

        fabAddListing.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreateListingFragment())
                .addToBackStack(null)
                .commit()
        }

        loadMyListings()
    }

    private fun loadMyListings() {
        progressBar.visibility = View.VISIBLE
        listingsRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = BookApiClient.getMyBooks(accessToken, mapOf("size" to "100"))
                val books = (result.data as? PaginatedResponse<BookDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    textListingsCount.text = "${books.size} listings"

                    if (books.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        listingsRecycler.visibility = View.VISIBLE
                        bookAdapter.updateData(books)
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

    private fun showDeleteConfirmation(book: BookDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Listing")
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

                val result = BookApiClient.deleteBook(accessToken, bookId)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Listing deleted successfully!", Toast.LENGTH_SHORT).show()
                        loadMyListings()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to delete listing", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        loadMyListings()
    }
}
