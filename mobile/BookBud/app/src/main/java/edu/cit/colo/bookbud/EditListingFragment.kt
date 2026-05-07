package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class EditListingFragment : Fragment() {

    private lateinit var editTitle: EditText
    private lateinit var editAuthor: EditText
    private lateinit var spinnerGenre: Spinner
    private lateinit var spinnerCondition: Spinner
    private lateinit var transactionTypeContainer: LinearLayout
    private lateinit var editPriceRent: EditText
    private lateinit var editPriceSale: EditText
    private lateinit var editDescription: EditText
    private lateinit var errorMessage: TextView
    private lateinit var btnSubmit: Button
    private lateinit var btnDelete: Button

    private var selectedTransactionType: String = "both"
    private var accessToken: String? = null
    private var bookId: String? = null
    private var book: BookDTO? = null

    companion object {
        private const val ARG_BOOK_ID = "book_id"

        fun newInstance(bookId: String): EditListingFragment {
            return EditListingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BOOK_ID, bookId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookId = it.getString(ARG_BOOK_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTitle = view.findViewById(R.id.editTitle)
        editAuthor = view.findViewById(R.id.editAuthor)
        spinnerGenre = view.findViewById(R.id.spinnerGenre)
        spinnerCondition = view.findViewById(R.id.spinnerCondition)
        transactionTypeContainer = view.findViewById(R.id.transactionTypeContainer)
        editPriceRent = view.findViewById(R.id.editPriceRent)
        editPriceSale = view.findViewById(R.id.editPriceSale)
        editDescription = view.findViewById(R.id.editDescription)
        errorMessage = view.findViewById(R.id.errorMessage)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Add delete button
        btnDelete = Button(requireContext()).apply {
            text = "Delete Listing"
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            setOnClickListener { showDeleteConfirmation() }
        }

        // Add delete button to the form container
        val formContainer = view.findViewById<LinearLayout>(R.id.formContainer)
        formContainer?.addView(btnDelete)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Setup spinners
        setupSpinners()

        // Setup transaction type buttons
        setupTransactionTypeButtons()

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitUpdate()
        }

        // Load book data
        loadBookData()
    }

    private fun setupSpinners() {
        val genres = arrayOf("Fiction", "Fantasy", "Drama", "Mystery", "Thriller", "Biography", "Self-Help", "Classic")
        val conditions = arrayOf("New", "Like New", "Good", "Fair", "Poor")

        val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        val conditionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions)

        spinnerGenre.adapter = genreAdapter
        spinnerCondition.adapter = conditionAdapter
    }

    private fun setupTransactionTypeButtons() {
        transactionTypeContainer.removeAllViews()

        val types = listOf("Rent", "Sale", "Both")
        val typeValues = listOf("rent", "sale", "both")

        types.forEachIndexed { index, type ->
            val btn = Button(requireContext()).apply {
                text = type
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    if (index > 0) marginStart = 8
                    if (index < types.size - 1) marginEnd = 8
                }

                setOnClickListener {
                    selectedTransactionType = typeValues[index]
                    updateTransactionTypeButtonStates()
                }
            }
            transactionTypeContainer.addView(btn)
        }
    }

    private fun updateTransactionTypeButtonStates() {
        for (i in 0 until transactionTypeContainer.childCount) {
            val btn = transactionTypeContainer.getChildAt(i) as? Button ?: continue
            val btnType = when (btn.text.toString()) {
                "Rent" -> "rent"
                "Sale" -> "sale"
                "Both" -> "both"
                else -> ""
            }
            if (btnType == selectedTransactionType) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun loadBookData() {
        val bookId = this.bookId ?: run {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        Thread {
            try {
                val result = BookApiClient.getBookById(bookId)
                val book = result.data as? BookDTO

                requireActivity().runOnUiThread {
                    if (book != null) {
                        this.book = book
                        populateForm(book)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load book", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }.start()
    }

    private fun populateForm(book: BookDTO) {
        editTitle.setText(book.title ?: "")
        editAuthor.setText(book.author ?: "")
        editDescription.setText(book.description ?: "")
        editPriceRent.setText(book.priceRent?.toString() ?: "")
        editPriceSale.setText(book.priceSale?.toString() ?: "")

        // Set genre spinner
        val genres = arrayOf("Fiction", "Fantasy", "Drama", "Mystery", "Thriller", "Biography", "Self-Help", "Classic")
        val genreIndex = genres.indexOfFirst { it.equals(book.genre, ignoreCase = true) }
        if (genreIndex >= 0) {
            spinnerGenre.setSelection(genreIndex)
        }

        // Set condition spinner
        val conditions = arrayOf("New", "Like New", "Good", "Fair", "Poor")
        val conditionIndex = conditions.indexOfFirst { it.equals(book.condition, ignoreCase = true) }
        if (conditionIndex >= 0) {
            spinnerCondition.setSelection(conditionIndex)
        }

        // Set transaction type
        selectedTransactionType = book.transactionType?.lowercase() ?: "both"
        updateTransactionTypeButtonStates()
    }

    private fun submitUpdate() {
        val bookId = this.bookId ?: return
        val title = editTitle.text.toString().trim()
        val author = editAuthor.text.toString().trim()
        val genre = spinnerGenre.selectedItem.toString()
        val condition = spinnerCondition.selectedItem.toString()
        val description = editDescription.text.toString().trim()
        val priceRentStr = editPriceRent.text.toString().trim()
        val priceSaleStr = editPriceSale.text.toString().trim()

        // Validation
        val error = validateForm(title, author, condition, priceRentStr, priceSaleStr)
        if (error != null) {
            errorMessage.text = error
            errorMessage.visibility = View.VISIBLE
            return
        }

        errorMessage.visibility = View.GONE
        btnSubmit.isEnabled = false

        Thread {
            try {
                val accessToken = this.accessToken ?: run {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                    }
                    return@Thread
                }

                val priceRent = if (selectedTransactionType == "sale") null else priceRentStr.toDoubleOrNull()
                val priceSale = if (selectedTransactionType == "rent") null else priceSaleStr.toDoubleOrNull()

                val updateRequest = UpdateBookRequest(
                    title = title,
                    author = author,
                    genre = genre,
                    description = description.ifEmpty { null },
                    condition = condition,
                    transactionType = selectedTransactionType,
                    priceRent = priceRent,
                    priceSale = priceSale
                )

                val result = BookApiClient.updateBook(accessToken, bookId, updateRequest)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Listing updated successfully!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        errorMessage.text = result.message ?: "Failed to update listing"
                        errorMessage.visibility = View.VISIBLE
                        btnSubmit.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    errorMessage.text = "Error: ${e.message}"
                    errorMessage.visibility = View.VISIBLE
                    btnSubmit.isEnabled = true
                }
            }
        }.start()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this book listing? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBook() {
        val bookId = this.bookId ?: return

        btnDelete.isEnabled = false
        btnSubmit.isEnabled = false

        Thread {
            try {
                val accessToken = this.accessToken ?: run {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show()
                        btnDelete.isEnabled = true
                        btnSubmit.isEnabled = true
                    }
                    return@Thread
                }

                val result = BookApiClient.deleteBook(accessToken, bookId)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Listing deleted successfully!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to delete listing", Toast.LENGTH_SHORT).show()
                        btnDelete.isEnabled = true
                        btnSubmit.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnDelete.isEnabled = true
                    btnSubmit.isEnabled = true
                }
            }
        }.start()
    }

    private fun validateForm(
        title: String,
        author: String,
        condition: String,
        priceRent: String,
        priceSale: String
    ): String? {
        if (title.isEmpty()) return "Title is required"
        if (author.isEmpty()) return "Author is required"
        if (condition.isEmpty()) return "Condition is required"

        val needsRent = selectedTransactionType == "rent" || selectedTransactionType == "both"
        val needsSale = selectedTransactionType == "sale" || selectedTransactionType == "both"

        if (needsRent && (priceRent.isEmpty() || priceRent.toDoubleOrNull() ?: 0.0 <= 0)) {
            return "Rental price must be greater than 0"
        }

        if (needsSale && (priceSale.isEmpty() || priceSale.toDoubleOrNull() ?: 0.0 <= 0)) {
            return "Sale price must be greater than 0"
        }

        return null
    }
}
