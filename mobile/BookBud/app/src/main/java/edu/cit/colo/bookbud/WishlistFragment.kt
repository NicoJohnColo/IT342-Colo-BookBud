package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WishlistFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var wishlistRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var textWishlistTitle: TextView
    private lateinit var textWishlistCount: TextView
    private var accessToken: String? = null
    private lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressWishlist)
        wishlistRecycler = view.findViewById(R.id.wishlistRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        textWishlistTitle = view.findViewById(R.id.textWishlistTitle)
        textWishlistCount = view.findViewById(R.id.textWishlistCount)

        // Back button - navigate to Profile
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        wishlistAdapter = WishlistAdapter(
            onRemove = { item -> removeFromWishlist(item) },
            onClick = { item ->
                Toast.makeText(requireContext(), "Book: ${item.book?.title}", Toast.LENGTH_SHORT).show()
            }
        )
        wishlistRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        wishlistRecycler.adapter = wishlistAdapter

        loadWishlist()
    }

    private fun loadWishlist() {
        progressBar.visibility = View.VISIBLE
        wishlistRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = WishlistApiClient.getMyWishlist(accessToken)
                val items = result.data ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    textWishlistCount.text = "${items.size} items"

                    if (items.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        wishlistRecycler.visibility = View.VISIBLE
                        wishlistAdapter.updateData(items)
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

    private fun removeFromWishlist(item: WishlistItemDTO) {
        val wishlistId = item.wishlistId ?: return
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                WishlistApiClient.removeFromWishlist(accessToken, wishlistId)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show()
                    loadWishlist()
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
        loadWishlist()
    }
}
