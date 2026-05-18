package com.example.bookbud.features.books.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.CreateBookRequest
import com.example.bookbud.shared.network.BookApiClient
import kotlinx.coroutines.*

class CreateListingFragment : Fragment() {
    private var selectedTransactionType = "both" // Default selection
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_create_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTitle = view.findViewById<EditText>(R.id.editTitle)
        val editAuthor = view.findViewById<EditText>(R.id.editAuthor)
        val editISBN = view.findViewById<EditText>(R.id.editISBN)
        val editPriceRent = view.findViewById<EditText>(R.id.editPriceRent)
        val editPriceSale = view.findViewById<EditText>(R.id.editPriceSale)
        val editDescription = view.findViewById<EditText>(R.id.editDescription)
        
        val spinnerGenre = view.findViewById<Spinner>(R.id.spinnerGenre)
        val spinnerCondition = view.findViewById<Spinner>(R.id.spinnerCondition)
        val btnAutoFill = view.findViewById<Button>(R.id.btnAutoFill)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val errorText = view.findViewById<TextView>(R.id.errorMessage)

        // Setup Spinners
        val genres = listOf("Fiction", "Fantasy", "Drama", "Mystery", "Thriller", "Biography", "Self-Help", "Classic")
        val conditions = listOf("New", "Like New", "Good", "Fair", "Poor")

        spinnerGenre?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        spinnerCondition?.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions)

        // Setup Transaction Type Toggle Buttons programmatically
        val toggleContainer = view.findViewById<LinearLayout>(R.id.transactionTypeContainer)
        setupTransactionTypeToggles(toggleContainer, editPriceRent, editPriceSale)

        // ISBN Auto-fill listener
        btnAutoFill?.setOnClickListener {
            val isbn = editISBN?.text?.toString()?.trim() ?: ""
            if (isbn.isEmpty()) {
                errorText?.visibility = View.VISIBLE
                errorText?.text = "Please enter an ISBN code first"
                return@setOnClickListener
            }

            btnAutoFill.text = "Loading..."
            btnAutoFill.isEnabled = false
            errorText?.visibility = View.GONE

            lifecycleScope.launch {
                val results = withContext(Dispatchers.IO) { BookApiClient.searchExternalBooks(isbn) }
                withContext(Dispatchers.Main) {
                    btnAutoFill.text = "Auto-Fill"
                    btnAutoFill.isEnabled = true

                    if (!results.isNullOrEmpty()) {
                        val book = results[0]
                        editTitle?.setText(book.title ?: "")
                        editAuthor?.setText(book.authors?.joinToString(", ") ?: "")
                        editDescription?.setText(book.description ?: "")
                        
                        // Map category to genre if possible
                        val cat = book.categories?.firstOrNull() ?: ""
                        val genreIdx = genres.indexOfFirst { it.equals(cat, ignoreCase = true) }
                        if (genreIdx >= 0) {
                            spinnerGenre?.setSelection(genreIdx)
                        }
                    } else {
                        errorText?.visibility = View.VISIBLE
                        errorText?.text = "No book found for this ISBN. Enter details manually."
                    }
                }
            }
        }

        // Submit listener
        btnSubmit?.setOnClickListener {
            val title = editTitle?.text?.toString()?.trim() ?: ""
            val author = editAuthor?.text?.toString()?.trim() ?: ""
            val description = editDescription?.text?.toString()?.trim() ?: ""
            val isbn = editISBN?.text?.toString()?.trim() ?: ""
            val genre = spinnerGenre?.selectedItem?.toString() ?: "Fiction"
            val condition = spinnerCondition?.selectedItem?.toString() ?: "Good"

            if (title.isEmpty() || author.isEmpty()) {
                errorText?.visibility = View.VISIBLE
                errorText?.text = "Title and Author are required."
                return@setOnClickListener
            }

            val rentVal = editPriceRent?.text?.toString()?.toDoubleOrNull()
            val saleVal = editPriceSale?.text?.toString()?.toDoubleOrNull()

            val request = CreateBookRequest(
                title = title,
                author = author,
                genre = genre,
                condition = condition,
                transactionType = selectedTransactionType,
                priceRent = if (selectedTransactionType == "sale") null else rentVal,
                priceSale = if (selectedTransactionType == "rent") null else saleVal,
                description = if (description.isEmpty()) null else description
            )

            errorText?.visibility = View.GONE
            btnSubmit.text = "Publishing..."
            btnSubmit.isEnabled = false

            lifecycleScope.launch {
                val token = TokenManager.getAccessToken() ?: ""
                val success = withContext(Dispatchers.IO) { BookApiClient.createBook(token, request) }
                withContext(Dispatchers.Main) {
                    btnSubmit.text = "Publish Listing"
                    btnSubmit.isEnabled = true

                    if (success != null) {
                        Toast.makeText(requireContext(), "Listing Published Successfully!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        errorText?.visibility = View.VISIBLE
                        errorText?.text = "Failed to publish listing. Please check connection."
                    }
                }
            }
        }
    }

    private fun setupTransactionTypeToggles(
        container: LinearLayout?,
        editPriceRent: EditText?,
        editPriceSale: EditText?
    ) {
        container?.removeAllViews()
        val options = listOf("rent", "sale", "both")
        val buttons = mutableListOf<Button>()

        options.forEach { opt ->
            val btn = Button(requireContext()).apply {
                text = opt.replaceFirstChar { it.uppercase() }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
                setOnClickListener {
                    selectedTransactionType = opt
                    buttons.forEach { b -> updateToggleStyle(b, b.text.toString().lowercase() == opt) }
                    
                    // Enable/disable price inputs dynamically
                    editPriceRent?.isEnabled = opt != "sale"
                    editPriceSale?.isEnabled = opt != "rent"
                }
            }
            buttons.add(btn)
            container?.addView(btn)
        }

        // Trigger initial style
        buttons.forEach { b -> updateToggleStyle(b, b.text.toString().lowercase() == selectedTransactionType) }
    }

    private fun updateToggleStyle(btn: Button, isActive: Boolean) {
        if (isActive) {
            btn.setBackgroundColor(resources.getColor(R.color.bb_orange, null))
            btn.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            btn.setBackgroundColor(resources.getColor(R.color.bb_card_white, null))
            btn.setTextColor(resources.getColor(R.color.bb_text_dark, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
