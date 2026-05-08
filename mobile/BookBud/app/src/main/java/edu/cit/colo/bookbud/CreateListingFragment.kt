package edu.cit.colo.bookbud

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.InputStream

class CreateListingFragment : Fragment() {

    private lateinit var editTitle: EditText
    private lateinit var editAuthor: EditText
    private lateinit var editISBN: EditText
    private lateinit var btnAutoFill: Button
    private lateinit var spinnerGenre: Spinner
    private lateinit var spinnerCondition: Spinner
    private lateinit var transactionTypeContainer: LinearLayout
    private lateinit var editPriceRent: EditText
    private lateinit var editPriceSale: EditText
    private lateinit var editDescription: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var imagePreview: ImageView
    private lateinit var errorMessage: TextView
    private lateinit var btnSubmit: Button

    private var selectedImageUri: Uri? = null
    private var selectedTransactionType: String = "both"
    private var accessToken: String? = null
    private var userId: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val bitmap = getBitmapFromUri(uri)
                imagePreview.setImageBitmap(bitmap)
                imagePreview.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_listing, container, false)
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
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        imagePreview = view.findViewById(R.id.imagePreview)
        errorMessage = view.findViewById(R.id.errorMessage)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)

        // Setup spinners
        setupSpinners()

        // Setup transaction type buttons
        setupTransactionTypeButtons()

        // Setup listeners
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            submitListing()
        }

        btnAutoFill.setOnClickListener {
            autoFillBookDetails()
        }
    }

    private fun setupSpinners() {
        val genres = arrayOf("Fiction", "Fantasy", "Drama", "Mystery", "Thriller", "Biography", "Self-Help", "Classic")
        val conditions = arrayOf("New", "Like New", "Good", "Fair", "Poor")

        val genreAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        val conditionAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions)

        spinnerGenre.adapter = genreAdapter
        spinnerCondition.adapter = conditionAdapter

        spinnerGenre.setSelection(2) // Default to "Drama"
        spinnerCondition.setSelection(2) // Default to "Good"
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

                if (index == 2) { // "Both" is default
                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }
            transactionTypeContainer.addView(btn)
        }
    }

    private fun updateTransactionTypeButtonStates() {
        for (i in 0 until transactionTypeContainer.childCount) {
            val btn = transactionTypeContainer.getChildAt(i) as? Button ?: continue
            if (btn.text.toString().lowercase() == selectedTransactionType || (selectedTransactionType == "both" && btn.text.toString() == "Both")) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun submitListing() {
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

                val userId = this.userId ?: run {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                    }
                    return@Thread
                }

                // Create book request
                val priceRent = if (selectedTransactionType == "sale") null else priceRentStr.toDoubleOrNull()
                val priceSale = if (selectedTransactionType == "rent") null else priceSaleStr.toDoubleOrNull()

                val createBookRequest = CreateBookRequest(
                    title = title,
                    author = author,
                    genre = genre,
                    description = description.ifEmpty { null },
                    condition = condition,
                    transactionType = selectedTransactionType,
                    priceRent = priceRent,
                    priceSale = priceSale
                )

                // Create book
                val bookResult = BookApiClient.createBook(accessToken, createBookRequest)
                val createdBook = bookResult.data as? BookDTO

                if (createdBook == null) {
                    requireActivity().runOnUiThread {
                        errorMessage.text = "Failed to create listing"
                        errorMessage.visibility = View.VISIBLE
                        btnSubmit.isEnabled = true
                    }
                    return@Thread
                }

                // Upload image if selected
                if (selectedImageUri != null) {
                    try {
                        val bookId = createdBook.bookId ?: throw Exception("Book ID is null")
                        val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                        val imageFile = inputStream?.readBytes() ?: byteArrayOf()
                        BookApiClient.uploadBookImage(accessToken, bookId, imageFile)
                    } catch (e: Exception) {
                        // Image upload failed but book was created - continue
                    }
                }

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Listing published successfully!", Toast.LENGTH_SHORT).show()
                    clearForm()
                    btnSubmit.isEnabled = true
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

    private fun validateForm(title: String, author: String, condition: String, priceRentStr: String, priceSaleStr: String): String? {
        if (title.isEmpty()) return "Title is required"
        if (author.isEmpty()) return "Author is required"
        if (condition.isEmpty()) return "Condition is required"

        val needsRent = selectedTransactionType == "rent" || selectedTransactionType == "both"
        val needsSale = selectedTransactionType == "sale" || selectedTransactionType == "both"

        if (needsRent && priceRentStr.isEmpty()) return "Rental price is required"
        if (needsSale && priceSaleStr.isEmpty()) return "Sale price is required"

        if (needsRent) {
            val rentPrice = priceRentStr.toDoubleOrNull()
            if (rentPrice == null || rentPrice <= 0.0) return "Rental price must be greater than 0"
        }

        if (needsSale) {
            val salePrice = priceSaleStr.toDoubleOrNull()
            if (salePrice == null || salePrice <= 0.0) return "Sale price must be greater than 0"
        }

        return null
    }

    private fun autoFillBookDetails() {
        val isbn = editISBN.text.toString().trim()
        if (isbn.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter ISBN to auto-fill", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                val bookData = searchExternalBooks(isbn)
                requireActivity().runOnUiThread {
                    if (bookData.isNotEmpty()) {
                        val book = bookData[0]
                        editTitle.setText(book.title ?: "")
                        editAuthor.setText(book.authors?.join(", ") ?: "")
                        editDescription.setText(book.description ?: "")
                        // Try to set genre if available
                        book.categories?.firstOrNull()?.let { genre ->
                            val genreArray = resources.getStringArray(android.R.array.book_genres)
                            val genreIndex = genreArray.indexOf(genre)
                            if (genreIndex >= 0) {
                                spinnerGenre.setSelection(genreIndex)
                            }
                        }
                        Toast.makeText(requireContext(), "Book details filled successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No book found for this ISBN", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch book details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun searchExternalBooks(isbn: String): List<Map<String, Any>> {
        // This would call the backend API for Google Books
        // For now, return empty list as placeholder
        return emptyList()
    }

    private fun clearForm() {
        editTitle.text.clear()
        editAuthor.text.clear()
        editDescription.text.clear()
        editPriceRent.text.clear()
        editPriceSale.text.clear()
        imagePreview.setImageBitmap(null)
        imagePreview.visibility = View.GONE
        selectedImageUri = null
        selectedTransactionType = "both"
        setupTransactionTypeButtons()
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
}
