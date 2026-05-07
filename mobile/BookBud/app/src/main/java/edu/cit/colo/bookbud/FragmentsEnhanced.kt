package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Enhanced BooksFragment with search, filter, and wishlist functionality
 */
class BooksFragment : Fragment() {

    private lateinit var searchBooks: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var booksScroll: ScrollView
    private lateinit var booksContainer: LinearLayout
    private lateinit var emptyMessage: TextView
    private var accessToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBooks = view.findViewById(R.id.searchBooks)
        progressBar = view.findViewById(R.id.progressBooks)
        booksScroll = view.findViewById(R.id.booksScroll)
        booksContainer = view.findViewById(R.id.booksContainer)
        emptyMessage = view.findViewById(R.id.emptyBooksMessage)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        searchBooks.setOnEditorActionListener { _, _, _ ->
            loadBooks(searchBooks.text.toString())
            true
        }

        loadBooks()
    }

    private fun loadBooks(query: String = "") {
        progressBar.visibility = View.VISIBLE
        booksScroll.visibility = View.GONE
        emptyMessage.visibility = View.GONE

        Thread {
            try {
                val params = mutableMapOf("size" to "100")
                if (query.isNotEmpty()) {
                    params["q"] = query
                }
                val result = BookApiClient.getAllBooks(params)
                val booksList = (result.data as? PaginatedResponse<BookDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (booksList.isEmpty()) {
                        emptyMessage.visibility = View.VISIBLE
                    } else {
                        booksScroll.visibility = View.VISIBLE
                        displayBooks(booksList)
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

    private fun displayBooks(books: List<BookDTO>) {
        booksContainer.removeAllViews()
        books.forEach { book ->
            booksContainer.addView(createBookCard(book))
        }
    }

    private fun createBookCard(book: BookDTO): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(12, 12, 12, 12)
            elevation = 2f

            // Book details in a row
            val detailsRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Title
            detailsRow.addView(TextView(requireContext()).apply {
                text = book.title ?: "Unknown"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                textStyle = android.graphics.Typeface.BOLD
            })

            // Author
            detailsRow.addView(TextView(requireContext()).apply {
                text = "by ${book.author ?: "Unknown"}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })

            // Condition & Genre
            detailsRow.addView(TextView(requireContext()).apply {
                text = "${book.condition ?: "N/A"} • ${book.genre ?: "N/A"}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })

            // Price info
            detailsRow.addView(TextView(requireContext()).apply {
                val priceText = when {
                    book.transactionType == "rent" -> "PHP ${book.priceRent ?: 0}/day"
                    book.transactionType == "sale" -> "PHP ${book.priceSale ?: 0}"
                    else -> "Rent: PHP ${book.priceRent ?: 0} • Sale: PHP ${book.priceSale ?: 0}"
                }
                text = priceText
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                textStyle = android.graphics.Typeface.BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            })

            addView(detailsRow)
        }
    }
}

/**
 * Enhanced TransactionsFragment with status filtering and actions
 */
class TransactionsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var transactionsScroll: ScrollView
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var emptyMessage: TextView
    private lateinit var statusTabsContainer: LinearLayout
    private var accessToken: String? = null
    private var allTransactions: List<TransactionDTO> = emptyList()
    private var currentFilter: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressTransactions)
        transactionsScroll = view.findViewById(R.id.transactionsScroll)
        transactionsContainer = view.findViewById(R.id.transactionsContainer)
        emptyMessage = view.findViewById(R.id.emptyTransactionsMessage)
        statusTabsContainer = view.findViewById(R.id.statusTabsContainer)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        setupStatusTabs()
        loadTransactions()
    }

    private fun setupStatusTabs() {
        val statuses = listOf("All", "Pending", "Active", "Completed", "Cancelled")
        val statusValues = listOf("all", "pending", "active", "completed", "cancelled")

        statusTabsContainer.removeAllViews()

        statuses.forEachIndexed { index, status ->
            val btn = Button(requireContext()).apply {
                text = status
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = if (index > 0) 8 else 0
                }
                setOnClickListener {
                    currentFilter = statusValues[index]
                    updateStatusTabStates()
                    filterTransactions()
                }

                if (index == 0) {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }
            statusTabsContainer.addView(btn)
        }
    }

    private fun updateStatusTabStates() {
        for (i in 0 until statusTabsContainer.childCount) {
            val btn = statusTabsContainer.getChildAt(i) as? Button ?: continue
            val isActive = btn.text.toString().lowercase() == currentFilter ||
                    (currentFilter == "all" && btn.text.toString() == "All")
            if (isActive) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            }
        }
    }

    private fun loadTransactions() {
        progressBar.visibility = View.VISIBLE
        transactionsScroll.visibility = View.GONE
        emptyMessage.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = TransactionApiClient.getMyTransactions(accessToken, mapOf("size" to "100"))
                allTransactions = (result.data as? PaginatedResponse<TransactionDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    filterTransactions()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun filterTransactions() {
        val filtered = if (currentFilter == "all") {
            allTransactions
        } else {
            allTransactions.filter { it.status?.lowercase() == currentFilter }
        }

        if (filtered.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            transactionsScroll.visibility = View.GONE
        } else {
            emptyMessage.visibility = View.GONE
            transactionsScroll.visibility = View.VISIBLE
            displayTransactions(filtered)
        }
    }

    private fun displayTransactions(transactions: List<TransactionDTO>) {
        transactionsContainer.removeAllViews()
        transactions.forEach { txn ->
            transactionsContainer.addView(createTransactionCard(txn))
        }
    }

    private fun createTransactionCard(txn: TransactionDTO): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(12, 12, 12, 12)
            elevation = 2f

            // Book title
            addView(TextView(requireContext()).apply {
                text = txn.bookTitle ?: "Unknown Book"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                textStyle = android.graphics.Typeface.BOLD
            })

            // Transaction details
            addView(TextView(requireContext()).apply {
                val details = listOf(
                    "Owner: ${txn.ownerUsername ?: "N/A"}",
                    "Renter: ${txn.renterUsername ?: "N/A"}",
                    "Amount: PHP ${txn.amount ?: 0}"
                ).joinToString(" • ")
                text = details
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            })

            // Status
            addView(TextView(requireContext()).apply {
                text = "Status: ${txn.status ?: "Unknown"}"
                textSize = 13f
                val statusColor = when (txn.status?.lowercase()) {
                    "completed" -> android.R.color.holo_green_dark
                    "active" -> android.R.color.holo_blue_dark
                    "pending" -> android.R.color.holo_orange_dark
                    "cancelled" -> android.R.color.holo_red_dark
                    else -> android.R.color.darker_gray
                }
                setTextColor(ContextCompat.getColor(context, statusColor))
                textStyle = android.graphics.Typeface.BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            })

            // Dates
            addView(TextView(requireContext()).apply {
                val dates = listOf(
                    txn.startDate?.take(10) ?: "N/A",
                    txn.endDate?.take(10) ?: "N/A"
                ).joinToString(" to ")
                text = "Period: $dates"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })
        }
    }
}

/**
 * Enhanced NotificationsFragment with read/delete actions
 */
class NotificationsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var notificationsScroll: ScrollView
    private lateinit var notificationsContainer: LinearLayout
    private lateinit var emptyMessage: TextView
    private var accessToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressNotifications)
        notificationsScroll = view.findViewById(R.id.notificationsScroll)
        notificationsContainer = view.findViewById(R.id.notificationsContainer)
        emptyMessage = view.findViewById(R.id.emptyNotificationsMessage)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        loadNotifications()
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        notificationsScroll.visibility = View.GONE
        emptyMessage.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = NotificationApiClient.getMyNotifications(accessToken)
                val notifications = (result.data as? List<NotificationDTO>) ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (notifications.isEmpty()) {
                        emptyMessage.visibility = View.VISIBLE
                    } else {
                        notificationsScroll.visibility = View.VISIBLE
                        displayNotifications(notifications)
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

    private fun displayNotifications(notifications: List<NotificationDTO>) {
        notificationsContainer.removeAllViews()
        notifications.forEach { notif ->
            notificationsContainer.addView(createNotificationCard(notif))
        }
    }

    private fun createNotificationCard(notif: NotificationDTO): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(12, 12, 12, 12)
            elevation = 2f

            // Message
            addView(TextView(requireContext()).apply {
                text = notif.message ?: "No message"
                textSize = 14f
                setTextColor(if (notif.isRead) android.graphics.Color.GRAY else android.graphics.Color.BLACK)
                textStyle = if (notif.isRead) android.graphics.Typeface.NORMAL else android.graphics.Typeface.BOLD
            })

            // Date
            addView(TextView(requireContext()).apply {
                text = notif.createdAt?.take(10) ?: "Unknown"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 6 }
            })

            // Action buttons
            val actionRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 10 }
            }

            if (!notif.isRead) {
                actionRow.addView(Button(requireContext()).apply {
                    text = "Mark Read"
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply { marginEnd = 8 }
                    setOnClickListener {
                        markNotificationRead(notif.notificationId!!)
                    }
                })
            }

            actionRow.addView(Button(requireContext()).apply {
                text = "Delete"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setOnClickListener {
                    deleteNotification(notif.notificationId!!)
                }
            })

            addView(actionRow)
        }
    }

    private fun markNotificationRead(notificationId: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                NotificationApiClient.markAsRead(accessToken, notificationId)
                loadNotifications()
            } catch (e: Exception) {
                // Error silently
            }
        }.start()
    }

    private fun deleteNotification(notificationId: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                NotificationApiClient.deleteNotification(accessToken, notificationId)
                loadNotifications()
            } catch (e: Exception) {
                // Error silently
            }
        }.start()
    }
}

/**
 * Enhanced ProfileFragment with user info and stats
 */
class ProfileFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var profileScroll: ScrollView
    private lateinit var profileContainer: LinearLayout
    private lateinit var emptyMessage: TextView
    private var accessToken: String? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressProfile)
        profileScroll = view.findViewById(R.id.profileScroll)
        profileContainer = view.findViewById(R.id.profileContainer)
        emptyMessage = view.findViewById(R.id.emptyProfileMessage)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)

        loadProfile()
    }

    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        profileScroll.visibility = View.GONE
        emptyMessage.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val userId = this.userId ?: return@Thread

                val result = UserApiClient.getUserProfile(accessToken, userId)
                val profile = (result.data as? UserProfileDTO)

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (profile == null) {
                        emptyMessage.visibility = View.VISIBLE
                    } else {
                        profileScroll.visibility = View.VISIBLE
                        displayProfile(profile)
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

    private fun displayProfile(profile: UserProfileDTO) {
        profileContainer.removeAllViews()

        // Profile header
        profileContainer.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            elevation = 2f

            // Username
            addView(TextView(requireContext()).apply {
                text = profile.username ?: "User"
                textSize = 24f
                setTextColor(android.graphics.Color.BLACK)
                textStyle = android.graphics.Typeface.BOLD
            })

            // Rating
            addView(TextView(requireContext()).apply {
                text = "Rating: ${profile.rating ?: "N/A"}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            })

            // Member since
            addView(TextView(requireContext()).apply {
                text = "Member since ${profile.createdAt?.take(10) ?: "Unknown"}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })
        })

        // Contact info
        profileContainer.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            elevation = 2f

            addView(TextView(requireContext()).apply {
                text = "Contact Information"
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
                textStyle = android.graphics.Typeface.BOLD
            })

            if (!profile.facebookUrl.isNullOrEmpty()) {
                addView(TextView(requireContext()).apply {
                    text = "Facebook: ${profile.facebookUrl}"
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 8 }
                })
            }

            if (!profile.messenger.isNullOrEmpty()) {
                addView(TextView(requireContext()).apply {
                    text = "Messenger: ${profile.messenger}"
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 4 }
                })
            }

            if (!profile.mobileNumber.isNullOrEmpty()) {
                addView(TextView(requireContext()).apply {
                    text = "Phone: ${profile.mobileNumber}"
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 4 }
                })
            }
        })
    }
}
