package com.example.bookbud.features.books.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ImageView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.BookApiClient
import com.example.bookbud.shared.network.TransactionApiClient
import com.example.bookbud.shared.network.PaymentApiClient
import com.example.bookbud.shared.network.WishlistApiClient
import com.example.bookbud.shared.models.CreateTransactionRequest
import com.example.bookbud.shared.models.BookDTO
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BooksFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBooks(view)
    }

    private fun loadBooks(view: View) {
        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: ""
            val wishlistItems = if (token.isNotEmpty()) {
                WishlistApiClient.getMyWishlist(token)
            } else {
                emptyList()
            }
            val wishlistBookIds = wishlistItems.mapNotNull { it.book?.bookId }.toSet()

            val books = BookApiClient.getAllBooks()
            withContext(Dispatchers.Main) {
                val container = view.findViewById<LinearLayout>(R.id.booksContainer)
                container?.removeAllViews()
                
                books?.forEach { book ->
                    val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_book, container, false)
                    
                    itemView.findViewById<TextView>(R.id.bookTitle)?.text = book.title
                    itemView.findViewById<TextView>(R.id.bookAuthor)?.text = book.author
                    itemView.findViewById<TextView>(R.id.bookGenre)?.text = "${book.genre ?: "Category"} • ${book.condition ?: "Good"}"
                    
                    val price = book.priceSale ?: book.priceRent ?: 0.0
                    itemView.findViewById<TextView>(R.id.bookPrice)?.text = String.format("PHP %.2f", price)
                    
                    val typeTv = itemView.findViewById<TextView>(R.id.bookTransactionType)
                    val rentAvailable = book.priceRent != null && book.priceRent > 0
                    val saleAvailable = book.priceSale != null && book.priceSale > 0
                    
                    typeTv?.text = when {
                        rentAvailable && saleAvailable -> "Both"
                        rentAvailable -> "Rent"
                        else -> "Sale"
                    }

                    // Wishlist Icon & Handler
                    val heartIcon = itemView.findViewById<TextView>(R.id.bookWishlistIcon)
                    val isInWishlist = wishlistBookIds.contains(book.bookId)
                    if (isInWishlist) {
                        heartIcon?.text = "♥"
                        heartIcon?.setTextColor(resources.getColor(R.color.bb_orange, null))
                    } else {
                        heartIcon?.text = "♡"
                        heartIcon?.setTextColor(resources.getColor(R.color.bb_text_muted, null))
                    }

                    heartIcon?.setOnClickListener {
                        lifecycleScope.launch {
                            val currentToken = TokenManager.getAccessToken()
                            if (currentToken.isNullOrEmpty()) {
                                Toast.makeText(requireContext(), "Please login to manage wishlist", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            
                            val isCurrentlyInWishlist = wishlistBookIds.contains(book.bookId)
                            if (isCurrentlyInWishlist) {
                                val itemId = wishlistItems.firstOrNull { it.book?.bookId == book.bookId }?.wishlistId
                                if (itemId != null) {
                                    WishlistApiClient.removeFromWishlist(currentToken, itemId)
                                    Toast.makeText(requireContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                WishlistApiClient.addToWishlist(currentToken, book.bookId)
                                Toast.makeText(requireContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show()
                            }
                            loadBooks(view)
                        }
                    }

                    // Detail Dialog & Checkout click handler
                    itemView.setOnClickListener {
                        showBookDetailsDialog(book, token) {
                            loadBooks(view)
                        }
                    }

                    container?.addView(itemView)
                }
            }
        }
    }

    private fun showBookDetailsDialog(book: BookDTO, token: String, onFinished: () -> Unit) {
        val currentUserId = TokenManager.getUserId() ?: ""
        val isOwner = book.ownerId == currentUserId

        val detailsBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(book.title)
            .setMessage(
                "Author: ${book.author}\n" +
                "Genre: ${book.genre ?: "General"}\n" +
                "Condition: ${book.condition ?: "Good"}\n" +
                "Status: ${book.status ?: "Available"}\n\n" +
                "Description: ${book.description ?: "No description provided."}\n\n" +
                (book.priceSale?.let { "Sale Price: PHP ${String.format("%.2f", it)}\n" } ?: "") +
                (book.priceRent?.let { "Rental Price: PHP ${String.format("%.2f", it)}/day\n" } ?: "")
            )

        if (isOwner) {
            detailsBuilder.setPositiveButton("Close", null)
            detailsBuilder.setNeutralButton("Your Listing") { _, _ -> }
            detailsBuilder.show()
        } else if (book.status?.uppercase() == "SOLD" || book.status?.uppercase() == "RENTED") {
            detailsBuilder.setPositiveButton("Close", null)
            detailsBuilder.setNeutralButton("Unavailable") { _, _ -> }
            detailsBuilder.show()
        } else {
            val rentAvailable = book.priceRent != null && book.priceRent > 0
            val saleAvailable = book.priceSale != null && book.priceSale > 0

            if (saleAvailable) {
                detailsBuilder.setPositiveButton("Buy Now") { _, _ ->
                    showPaymentSelectionDialog(book, token, false, onFinished)
                }
            }
            if (rentAvailable) {
                detailsBuilder.setNegativeButton("Rent Now") { _, _ ->
                    showPaymentSelectionDialog(book, token, true, onFinished)
                }
            }
            detailsBuilder.setNeutralButton("Close", null)
            detailsBuilder.show()
        }
    }

    private fun showPaymentSelectionDialog(book: BookDTO, token: String, initialIsRent: Boolean, onFinished: () -> Unit) {
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to proceed with transaction", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_checkout_details, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.checkoutDialogTitle)
        val tvBookTitle = dialogView.findViewById<TextView>(R.id.checkoutBookTitle)
        val tvBookAuthor = dialogView.findViewById<TextView>(R.id.checkoutBookAuthor)
        val tvSellerBadge = dialogView.findViewById<TextView>(R.id.checkoutSellerBadge)
        val tvPriceDisplay = dialogView.findViewById<TextView>(R.id.checkoutPriceDisplay)
        
        val toggleContainer = dialogView.findViewById<View>(R.id.checkoutTypeToggleContainer)
        val btnToggleRent = dialogView.findViewById<TextView>(R.id.btnToggleRent)
        val btnToggleBuy = dialogView.findViewById<TextView>(R.id.btnToggleBuy)
        
        val layoutRentDates = dialogView.findViewById<View>(R.id.layoutRentDates)
        val etStartDate = dialogView.findViewById<EditText>(R.id.checkoutStartDate)
        val etEndDate = dialogView.findViewById<EditText>(R.id.checkoutEndDate)
        
        val btnPaymentCash = dialogView.findViewById<View>(R.id.btnPaymentCash)
        val btnPaymentCard = dialogView.findViewById<View>(R.id.btnPaymentCard)
        
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCheckoutCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnCheckoutConfirm)

        tvBookTitle.text = book.title
        tvBookAuthor.text = book.author
        
        val currentUserId = TokenManager.getUserId() ?: ""
        val isOwned = book.ownerId == currentUserId
        if (isOwned) {
            tvSellerBadge.text = "Your Listing"
            tvSellerBadge.setTextColor(resources.getColor(R.color.bb_badge_hot_text, null))
            tvSellerBadge.setBackgroundResource(R.color.bb_badge_hot_bg)
        } else {
            tvSellerBadge.text = "Other Seller"
            tvSellerBadge.setTextColor(resources.getColor(R.color.bb_badge_topseller_text, null))
            tvSellerBadge.setBackgroundResource(R.drawable.bg_badge_other_seller)
        }

        val tvCoverLetter = dialogView.findViewById<TextView>(R.id.checkoutCoverLetter)
        val ivBookCover = dialogView.findViewById<ImageView>(R.id.checkoutBookCover)
        
        tvCoverLetter.text = book.title.take(1).uppercase()
        tvCoverLetter.visibility = View.VISIBLE
        ivBookCover.visibility = View.GONE

        var isRentState = initialIsRent
        var paymentMethodState = "Cash"

        fun updateModeUI() {
            if (isRentState) {
                tvTitle.text = "Rent This Book"
                btnToggleRent.setBackgroundResource(R.color.bb_orange)
                btnToggleRent.setTextColor(resources.getColor(R.color.bb_text_light, null))
                btnToggleBuy.setBackgroundResource(android.R.color.transparent)
                btnToggleBuy.setTextColor(resources.getColor(R.color.bb_text_muted, null))
                
                tvPriceDisplay.text = String.format("Rental Price / day: PHP %.2f", book.priceRent ?: 0.0)
                layoutRentDates.visibility = View.VISIBLE
                
                btnConfirm.text = String.format("Confirm Rental - PHP %.2f/day", book.priceRent ?: 0.0)
            } else {
                tvTitle.text = "Buy This Book"
                btnToggleBuy.setBackgroundResource(R.color.bb_orange)
                btnToggleBuy.setTextColor(resources.getColor(R.color.bb_text_light, null))
                btnToggleRent.setBackgroundResource(android.R.color.transparent)
                btnToggleRent.setTextColor(resources.getColor(R.color.bb_text_muted, null))
                
                tvPriceDisplay.text = String.format("Sale Price: PHP %.2f", book.priceSale ?: 0.0)
                layoutRentDates.visibility = View.GONE
                
                btnConfirm.text = String.format("Confirm Purchase - PHP %.2f", book.priceSale ?: 0.0)
            }
        }

        fun updatePaymentUI() {
            if (paymentMethodState == "Cash") {
                btnPaymentCash.setBackgroundResource(R.drawable.bg_payment_method_selected)
                btnPaymentCard.setBackgroundResource(R.drawable.bg_payment_method_unselected)
            } else {
                btnPaymentCard.setBackgroundResource(R.drawable.bg_payment_method_selected)
                btnPaymentCash.setBackgroundResource(R.drawable.bg_payment_method_unselected)
            }
        }

        val rentAvailable = book.priceRent != null && book.priceRent > 0
        val saleAvailable = book.priceSale != null && book.priceSale > 0

        if (rentAvailable && saleAvailable) {
            toggleContainer.visibility = View.VISIBLE
            btnToggleRent.setOnClickListener {
                isRentState = true
                updateModeUI()
            }
            btnToggleBuy.setOnClickListener {
                isRentState = false
                updateModeUI()
            }
        } else {
            toggleContainer.visibility = View.GONE
            isRentState = rentAvailable
        }

        updateModeUI()
        updatePaymentUI()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayCalendar = Calendar.getInstance()
        etStartDate.setText(sdf.format(todayCalendar.time))
        
        val endCalendar = Calendar.getInstance()
        endCalendar.add(Calendar.DAY_OF_YEAR, 7)
        etEndDate.setText(sdf.format(endCalendar.time))

        etStartDate.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    etStartDate.setText(sdf.format(cal.time))
                },
                todayCalendar.get(Calendar.YEAR),
                todayCalendar.get(Calendar.MONTH),
                todayCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        etEndDate.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    etEndDate.setText(sdf.format(cal.time))
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnPaymentCash.setOnClickListener {
            paymentMethodState = "Cash"
            updatePaymentUI()
        }
        btnPaymentCard.setOnClickListener {
            paymentMethodState = "Stripe"
            updatePaymentUI()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            val startVal = etStartDate.text.toString()
            val endVal = etEndDate.text.toString()
            if (isRentState && (startVal.isEmpty() || endVal.isEmpty())) {
                Toast.makeText(requireContext(), "Please select rent dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            processCheckoutCustom(
                book = book,
                token = token,
                isRent = isRentState,
                startDateStr = startVal,
                endDateStr = endVal,
                paymentMethod = paymentMethodState,
                onFinished = onFinished
            )
        }

        dialog.show()
    }

    private fun processCheckoutCustom(
        book: BookDTO,
        token: String,
        isRent: Boolean,
        startDateStr: String,
        endDateStr: String,
        paymentMethod: String,
        onFinished: () -> Unit
    ) {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setMessage("Processing checkout transaction...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        lifecycleScope.launch {
            val req = CreateTransactionRequest(
                bookId = book.bookId,
                startDate = startDateStr,
                endDate = if (isRent) endDateStr else startDateStr,
                paymentMethod = if (paymentMethod == "Stripe") "Stripe_Card" else "Cash"
            )

            val transaction = TransactionApiClient.createTransaction(token, req)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (transaction != null) {
                    if (paymentMethod == "Stripe") {
                        val amount = if (isRent) book.priceRent ?: 0.0 else book.priceSale ?: 0.0
                        processStripePaymentCustom(transaction.transactionId, token, amount, onFinished)
                    } else {
                        Toast.makeText(requireContext(), "Checkout Successful! Please settle transaction via Cash.", Toast.LENGTH_LONG).show()
                        onFinished()
                    }
                } else {
                    Toast.makeText(requireContext(), "Transaction creation failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun processStripePaymentCustom(transactionId: String, token: String, amount: Double, onFinished: () -> Unit) {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setMessage("Initiating secure Stripe connection...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        lifecycleScope.launch {
            val response = PaymentApiClient.createPaymentIntent(token, transactionId)
            
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (response != null && response.clientSecret.isNotEmpty()) {
                    showStripePaymentModal(transactionId, token, onFinished)
                } else {
                    Toast.makeText(requireContext(), "Stripe payment initiation failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showStripePaymentModal(transactionId: String, token: String, onFinished: () -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stripe_payment, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCardNumber = dialogView.findViewById<EditText>(R.id.stripeCardNumber)
        val etCardExpiry = dialogView.findViewById<EditText>(R.id.stripeCardExpiry)
        val etCardCvc = dialogView.findViewById<EditText>(R.id.stripeCardCvc)
        val etCardZip = dialogView.findViewById<EditText>(R.id.stripeCardZip)
        
        val btnCancel = dialogView.findViewById<Button>(R.id.stripeBtnCancel)
        val btnClear = dialogView.findViewById<Button>(R.id.stripeBtnClear)
        val btnPay = dialogView.findViewById<Button>(R.id.stripeBtnPay)

        etCardExpiry.addTextChangedListener(object : android.text.TextWatcher {
            var lastLen = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val str = s.toString()
                if (str.length == 2 && lastLen < 2) {
                    s?.append("/")
                }
                lastLen = str.length
            }
        })

        etCardNumber.addTextChangedListener(object : android.text.TextWatcher {
            var lastLen = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val str = s.toString().replace(" ", "")
                if (str.length > 0 && str.length % 4 == 0 && s.toString().length > lastLen) {
                    if (str.length < 16) {
                        s?.append(" ")
                    }
                }
                lastLen = s.toString().length
            }
        })

        btnCancel.setOnClickListener {
            lifecycleScope.launch {
                TransactionApiClient.updateStatus(token, transactionId, "Cancelled")
            }
            dialog.dismiss()
            Toast.makeText(requireContext(), "Payment cancelled", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            etCardNumber.setText("")
            etCardExpiry.setText("")
            etCardCvc.setText("")
            etCardZip.setText("")
            Toast.makeText(requireContext(), "Card fields cleared", Toast.LENGTH_SHORT).show()
        }

        btnPay.setOnClickListener {
            val cardNo = etCardNumber.text.toString().replace(" ", "")
            val cardExp = etCardExpiry.text.toString()
            val cardCvc = etCardCvc.text.toString()

            if (cardNo.length < 16 || cardExp.length < 5 || cardCvc.length < 3) {
                Toast.makeText(requireContext(), "Please enter valid card details", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            val payProgress = AlertDialog.Builder(requireContext())
                .setMessage("Processing secure Stripe payment...")
                .setCancelable(false)
                .create()
            payProgress.show()

            lifecycleScope.launch {
                val success = PaymentApiClient.confirmPayment(token, transactionId)
                withContext(Dispatchers.Main) {
                    payProgress.dismiss()
                    if (success) {
                        Toast.makeText(requireContext(), "Payment Confirmed via Stripe Successfully!", Toast.LENGTH_LONG).show()
                        onFinished()
                    } else {
                        Toast.makeText(requireContext(), "Stripe payment confirmation failed.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
