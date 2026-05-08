package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BooksFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var booksRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchBooks: EditText
    private lateinit var resultsCount: TextView
    private var accessToken: String? = null
    private lateinit var bookAdapter: BookAdapter

    // Filter views
    private lateinit var filterAllTypes: TextView
    private lateinit var filterForRent: TextView
    private lateinit var filterForSale: TextView
    private lateinit var filterAllGenres: TextView
    private lateinit var filterFantasy: TextView
    private lateinit var filterFiction: TextView
    private lateinit var filterSelfHelp: TextView
    private lateinit var filterMystery: TextView

    private var selectedType = "all"
    private var selectedGenre = "all"
    private var allBooks: List<BookDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBooks)
        booksRecycler = view.findViewById(R.id.booksRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        searchBooks = view.findViewById(R.id.searchBooks)
        resultsCount = view.findViewById(R.id.resultsCount)

        // Filter chips
        filterAllTypes = view.findViewById(R.id.filterAllTypes)
        filterForRent = view.findViewById(R.id.filterForRent)
        filterForSale = view.findViewById(R.id.filterForSale)
        filterAllGenres = view.findViewById(R.id.filterAllGenres)
        filterFantasy = view.findViewById(R.id.filterFantasy)
        filterFiction = view.findViewById(R.id.filterFiction)
        filterSelfHelp = view.findViewById(R.id.filterSelfHelp)
        filterMystery = view.findViewById(R.id.filterMystery)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        bookAdapter = BookAdapter { book ->
            Toast.makeText(requireContext(), "Clicked: " + book.title, Toast.LENGTH_SHORT).show()
        }
        booksRecycler.layoutManager = LinearLayoutManager(requireContext())
        booksRecycler.adapter = bookAdapter

        setupFilters()
        loadBooks()
    }

    private fun setupFilters() {
        // Type filters
        filterAllTypes.setOnClickListener { setTypeFilter("all", filterAllTypes, listOf(filterForRent, filterForSale)) }
        filterForRent.setOnClickListener { setTypeFilter("rent", filterForRent, listOf(filterAllTypes, filterForSale)) }
        filterForSale.setOnClickListener { setTypeFilter("sale", filterForSale, listOf(filterAllTypes, filterForRent)) }

        // Genre filters
        filterAllGenres.setOnClickListener { setGenreFilter("all", filterAllGenres, listOf(filterFantasy, filterFiction, filterSelfHelp, filterMystery)) }
        filterFantasy.setOnClickListener { setGenreFilter("Fantasy", filterFantasy, listOf(filterAllGenres, filterFiction, filterSelfHelp, filterMystery)) }
        filterFiction.setOnClickListener { setGenreFilter("Fiction", filterFiction, listOf(filterAllGenres, filterFantasy, filterSelfHelp, filterMystery)) }
        filterSelfHelp.setOnClickListener { setGenreFilter("Self-Help", filterSelfHelp, listOf(filterAllGenres, filterFantasy, filterFiction, filterMystery)) }
        filterMystery.setOnClickListener { setGenreFilter("Mystery", filterMystery, listOf(filterAllGenres, filterFantasy, filterFiction, filterSelfHelp)) }
    }

    private fun setTypeFilter(type: String, selected: TextView, others: List<TextView>) {
        selectedType = type
        selected.setBackgroundResource(R.drawable.bg_badge_hot)
        selected.setTextColor(resources.getColor(android.R.color.white, null))
        others.forEach {
            it.setBackgroundResource(R.drawable.bg_badge_gray)
            it.setTextColor(resources.getColor(R.color.bb_text_dark, null))
        }
        applyFilters()
    }

    private fun setGenreFilter(genre: String, selected: TextView, others: List<TextView>) {
        selectedGenre = genre
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

        var filtered = allBooks.filter { book ->
            val matchesSearch = query.isEmpty() ||
                    book.title?.lowercase()?.contains(query) == true ||
                    book.author?.lowercase()?.contains(query) == true

            val matchesType = when (selectedType) {
                "rent" -> book.transactionType?.lowercase() == "rent" || book.transactionType?.lowercase() == "both"
                "sale" -> book.transactionType?.lowercase() == "sale" || book.transactionType?.lowercase() == "both"
                else -> true
            }

            val matchesGenre = selectedGenre == "all" || book.genre?.equals(selectedGenre, ignoreCase = true) == true

            matchesSearch && matchesType && matchesGenre
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
                val params = mutableMapOf("size" to "100")
                val result = BookApiClient.getAllBooks(params)
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
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}

class TransactionsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private var accessToken: String? = null
    private var userId: String? = null
    private lateinit var txnAdapter: TransactionAdapter

    // Tab views
    private lateinit var tabAll: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabActive: TextView
    private lateinit var tabCompleted: TextView
    private lateinit var tabCancelled: TextView

    private var selectedTab = "all"
    private var allTransactions: List<TransactionDTO> = emptyList()

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
        transactionsRecycler = view.findViewById(R.id.transactionsRecycler)
        emptyState = view.findViewById(R.id.emptyState)

        // Tabs
        tabAll = view.findViewById(R.id.tabAll)
        tabPending = view.findViewById(R.id.tabPending)
        tabActive = view.findViewById(R.id.tabActive)
        tabCompleted = view.findViewById(R.id.tabCompleted)
        tabCancelled = view.findViewById(R.id.tabCancelled)

        // Back button - navigate to Profile
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)

        txnAdapter = TransactionAdapter { txn, action ->
            when (action) {
                0 -> updateTransactionStatus(txn.transactionId!!, "Active")
                1 -> showContactInfoDialog(txn)
                2 -> showRatingDialog(txn)
            }
        }
        transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionsRecycler.adapter = txnAdapter

        setupTabs()
        loadTransactions()
    }

    private fun setupTabs() {
        tabAll.setOnClickListener { setTab("all", tabAll, listOf(tabPending, tabActive, tabCompleted, tabCancelled)) }
        tabPending.setOnClickListener { setTab("pending", tabPending, listOf(tabAll, tabActive, tabCompleted, tabCancelled)) }
        tabActive.setOnClickListener { setTab("active", tabActive, listOf(tabAll, tabPending, tabCompleted, tabCancelled)) }
        tabCompleted.setOnClickListener { setTab("completed", tabCompleted, listOf(tabAll, tabPending, tabActive, tabCancelled)) }
        tabCancelled.setOnClickListener { setTab("cancelled", tabCancelled, listOf(tabAll, tabPending, tabActive, tabCompleted)) }
    }

    private fun setTab(status: String, selected: TextView, others: List<TextView>) {
        selectedTab = status
        selected.setBackgroundResource(R.drawable.bg_badge_hot)
        selected.setTextColor(resources.getColor(android.R.color.white, null))
        others.forEach {
            it.setBackgroundResource(R.drawable.bg_badge_gray)
            it.setTextColor(resources.getColor(R.color.bb_text_dark, null))
        }
        applyTabFilter()
    }

    private fun applyTabFilter() {
        val filtered = if (selectedTab == "all") {
            allTransactions
        } else {
            allTransactions.filter { it.status?.lowercase() == selectedTab }
        }

        txnAdapter.updateData(filtered)
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        transactionsRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadTransactions() {
        progressBar.visibility = View.VISIBLE
        transactionsRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = TransactionApiClient.getMyTransactions(accessToken, mapOf("size" to "100"))
                allTransactions = (result.data as? PaginatedResponse<TransactionDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    applyTabFilter()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateTransactionStatus(transactionId: String, status: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                TransactionApiClient.updateTransactionStatus(accessToken, transactionId, status)
                loadTransactions()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Updated to " + status, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showContactInfoDialog(txn: TransactionDTO) {
        val isOwner = txn.userRole?.lowercase() == "owner"
        val counterpartId = if (isOwner) txn.userId else txn.ownerId
        val counterpartName = if (isOwner) txn.renterUsername else txn.ownerUsername
        val roleLabel = if (isOwner) "Renter" else "Owner"

        // Show loading dialog first
        val loadingDialog = AlertDialog.Builder(requireContext())
            .setTitle("Loading Contact Info")
            .setMessage("Fetching $counterpartName's contact details...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Fetch counterpart profile
        Thread {
            try {
                val accessToken = this.accessToken ?: run {
                    requireActivity().runOnUiThread {
                        loadingDialog.dismiss()
                        Toast.makeText(requireContext(), "Not authenticated", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val counterpartUserId = counterpartId ?: run {
                    requireActivity().runOnUiThread {
                        loadingDialog.dismiss()
                        Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val result = UserApiClient.getUserProfile(accessToken, counterpartUserId)
                val user = result.data as? UserProfileDTO

                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()

                    if (user != null) {
                        showContactInfoContentDialog(user, counterpartName ?: "User", roleLabel)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load contact info", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showContactInfoContentDialog(user: UserProfileDTO, name: String, roleLabel: String) {
        // Build contact info message
        val message = buildString {
            appendLine("$roleLabel: $name")
            appendLine()
            appendLine("Contact Information:")
            appendLine("• Mobile: ${user.mobileNumber ?: "Not provided"}")
            appendLine("• Messenger: ${user.messenger ?: "Not provided"}")
            appendLine("• Facebook: ${user.facebookUrl ?: "Not provided"}")
            appendLine()
            appendLine("Rating: ${user.rating ?: "N/A"} ★")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Contact Details")
            .setMessage(message)
            .setPositiveButton("Close", null)

        // Add action buttons if contact info exists
        val hasContact = !user.mobileNumber.isNullOrEmpty() ||
                        !user.messenger.isNullOrEmpty() ||
                        !user.facebookUrl.isNullOrEmpty()

        if (hasContact) {
            dialog.setNeutralButton("Copy Contact") { _, _ ->
                val contactInfo = buildString {
                    if (!user.mobileNumber.isNullOrEmpty()) appendLine("Mobile: ${user.mobileNumber}")
                    if (!user.messenger.isNullOrEmpty()) appendLine("Messenger: ${user.messenger}")
                    if (!user.facebookUrl.isNullOrEmpty()) appendLine("Facebook: ${user.facebookUrl}")
                }
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Contact Info", contactInfo.trim())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Contact info copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showRatingDialog(txn: TransactionDTO) {
        val ratingBar = RatingBar(requireContext()).apply {
            numStars = 5
            stepSize = 1.0f
            rating = 5.0f
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Rate User")
            .setMessage("How would you rate this transaction?")
            .setView(ratingBar)
            .setPositiveButton("Submit") { _, _ ->
                submitRating(txn.transactionId ?: return@setPositiveButton, ratingBar.rating.toDouble())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitRating(transactionId: String, rating: Double) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = TransactionApiClient.submitRating(accessToken, transactionId, rating)
                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Rating submitted", Toast.LENGTH_SHORT).show()
                        loadTransactions()
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${result.message}", Toast.LENGTH_SHORT).show()
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

class NotificationsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var notificationsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private var accessToken: String? = null
    private lateinit var notifAdapter: NotificationAdapter

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
        notificationsRecycler = view.findViewById(R.id.notificationsRecycler)
        emptyState = view.findViewById(R.id.emptyState)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        notifAdapter = NotificationAdapter(
            onMarkUnread = { notif ->
                markNotificationRead(notif.notificationId!!)
            },
            onDelete = { notif ->
                deleteNotification(notif.notificationId!!)
            }
        )
        notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        notificationsRecycler.adapter = notifAdapter

        loadNotifications()
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        notificationsRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = NotificationApiClient.getMyNotifications(accessToken)
                val notifications = (result.data as? List<NotificationDTO>) ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (notifications.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        notificationsRecycler.visibility = View.VISIBLE
                        notifAdapter.updateData(notifications)
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun markNotificationRead(notificationId: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                NotificationApiClient.markAsRead(accessToken, notificationId)
                loadNotifications()
            } catch (e: Exception) {
                // Error
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
                // Error
            }
        }.start()
    }
}

class ProfileFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var profileScroll: ScrollView
    private lateinit var emptyState: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnMyTransactions: Button
    private lateinit var btnMyWishlist: Button
    private lateinit var btnMyEarnings: Button
    private lateinit var btnAdminDashboard: Button

    // Header views
    private lateinit var avatarInitial: TextView
    private lateinit var textProfileUsername: TextView
    private lateinit var textProfileRating: TextView

    // Stats views
    private lateinit var statListings: TextView
    private lateinit var statCompleted: TextView
    private lateinit var statMemberSince: TextView

    // Account info views
    private lateinit var textAccountUsername: TextView

    // Contact info views
    private lateinit var textFacebookUrl: TextView
    private lateinit var textMessenger: TextView
    private lateinit var textMobile: TextView

    private var accessToken: String? = null
    private var userId: String? = null
    private var username: String? = null

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
        emptyState = view.findViewById(R.id.emptyState)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnMyTransactions = view.findViewById(R.id.btnMyTransactions)
        btnMyWishlist = view.findViewById(R.id.btnMyWishlist)
        btnMyEarnings = view.findViewById(R.id.btnMyEarnings)
        btnAdminDashboard = view.findViewById(R.id.btnAdminDashboard)

        // Header
        avatarInitial = view.findViewById(R.id.avatarInitial)
        textProfileUsername = view.findViewById(R.id.textProfileUsername)
        textProfileRating = view.findViewById(R.id.textProfileRating)

        // Stats
        statListings = view.findViewById(R.id.statListings)
        statCompleted = view.findViewById(R.id.statCompleted)
        statMemberSince = view.findViewById(R.id.statMemberSince)

        // Account
        textAccountUsername = view.findViewById(R.id.textAccountUsername)

        // Contact
        textFacebookUrl = view.findViewById(R.id.textFacebookUrl)
        textMessenger = view.findViewById(R.id.textMessenger)
        textMobile = view.findViewById(R.id.textMobile)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)
        username = prefs.getString("username", "User")

        // Set initial from username
        avatarInitial.text = username?.take(1)?.uppercase() ?: "U"
        textProfileUsername.text = username ?: "User"
        textAccountUsername.text = username ?: "-"

        // Edit Profile button
        view.findViewById<ImageButton>(R.id.btnEditProfile)?.setOnClickListener {
            openEditProfile()
        }

        btnLogout.setOnClickListener { logout() }
        btnMyTransactions.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TransactionsFragment())
                .addToBackStack(null)
                .commit()
        }
        btnMyWishlist.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WishlistFragment())
                .addToBackStack(null)
                .commit()
        }
        btnMyEarnings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PaymentsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Check if user is admin and show admin dashboard button
        if (TokenManager.isAdmin(requireContext())) {
            btnAdminDashboard.visibility = View.VISIBLE
            btnAdminDashboard.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminDashboardFragment())
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            btnAdminDashboard.visibility = View.GONE
        }

        loadProfile()
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        profileScroll.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val userId = this.userId ?: return@Thread
                val result = UserApiClient.getUserProfile(accessToken, userId)
                val user = result.data as? UserProfileDTO

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (user != null) {
                        profileScroll.visibility = View.VISIBLE
                        displayProfile(user)
                    } else {
                        emptyState.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Still show scroll view with cached data if available
                    profileScroll.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun displayProfile(user: UserProfileDTO) {
        user.role?.takeIf { it.isNotBlank() }?.let { role ->
            requireContext().getSharedPreferences("bookbud_prefs", 0)
                .edit()
                .putString("role", role)
                .apply()
        }

        if (TokenManager.isAdmin(requireContext())) {
            btnAdminDashboard.visibility = View.VISIBLE
            btnAdminDashboard.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminDashboardFragment())
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            btnAdminDashboard.visibility = View.GONE
        }

        // Update header
        avatarInitial.text = user.username?.take(1)?.uppercase() ?: "U"
        textProfileUsername.text = user.username ?: "User"

        // Calculate rating display from rating string
        val ratingStr = user.rating ?: "0.0"
        val rating = ratingStr.toDoubleOrNull() ?: 0.0
        val fullStars = rating.toInt()
        val halfStar = if (rating % 1 >= 0.5) 1 else 0
        val emptyStars = 5 - fullStars - halfStar
        val ratingStars = "★".repeat(fullStars) + if (halfStar > 0) "½" else "" + "☆".repeat(emptyStars)
        textProfileRating.text = "$ratingStars ${String.format("%.1f", rating)}"

        // Update stats (API doesn't provide these, using placeholders)
        statListings.text = "-"
        statCompleted.text = "-"

        // Member since - format date
        val memberSince = user.createdAt?.take(7)?.let { date ->
            val parts = date.split("-")
            if (parts.size >= 2) {
                val month = when(parts[1]) {
                    "01" -> "Jan"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Apr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Aug"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dec"
                    else -> "Jan"
                }
                "${month} '${parts[0].takeLast(2)}"
            } else "Jan '24"
        } ?: "Jan '24"
        statMemberSince.text = memberSince

        // Account info
        textAccountUsername.text = user.username ?: "-"

        // Contact info
        textFacebookUrl.text = if (user.facebookUrl.isNullOrEmpty()) "Not set" else user.facebookUrl
        textMessenger.text = if (user.messenger.isNullOrEmpty()) "Not set" else user.messenger
        textMobile.text = if (user.mobileNumber.isNullOrEmpty()) "Not set" else user.mobileNumber
    }

    private fun openEditProfile() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, EditProfileFragment())
            .addToBackStack(null)
            .commit()
    }
}
