package com.example.bookbud.features.admin.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.features.auth.presentation.activity.LoginActivity
import com.example.bookbud.features.admin.presentation.adapter.AdminBookAdapter
import com.example.bookbud.features.admin.presentation.adapter.AdminNotificationAdapter
import com.example.bookbud.features.admin.presentation.adapter.AdminTransactionAdapter
import com.example.bookbud.features.admin.presentation.adapter.AdminUserAdapter
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.AdminPlatformStatsDTO
import com.example.bookbud.shared.models.BookDTO
import com.example.bookbud.shared.models.NotificationDTO
import com.example.bookbud.shared.models.TransactionDTO
import com.example.bookbud.shared.models.UserProfileDTO
import com.example.bookbud.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class AdminDashboardFragment : Fragment() {
    private lateinit var progressAdmin: ProgressBar
    private lateinit var adminStatsContainer: LinearLayout
    private lateinit var quickAccessContainer: LinearLayout
    private lateinit var textAdminDate: TextView
    private lateinit var btnAdminLogout: View
    private lateinit var adminDashboardScroll: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressAdmin = view.findViewById(R.id.progressAdmin)
        adminStatsContainer = view.findViewById(R.id.adminStatsContainer)
        quickAccessContainer = view.findViewById(R.id.quickAccessContainer)
        textAdminDate = view.findViewById(R.id.textAdminDate)
        btnAdminLogout = view.findViewById(R.id.btnAdminLogout)
        adminDashboardScroll = view.findViewById(R.id.adminDashboardScroll)

        btnAdminLogout.setOnClickListener {
            TokenManager.clearAll()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        loadAdminOverview()
    }

    private fun loadAdminOverview() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    showError("Not authenticated")
                    return@launch
                }

                val statsDeferred = async(Dispatchers.IO) { RetrofitClient.adminApi.getPlatformStats("Bearer $token") }
                val booksDeferred = async(Dispatchers.IO) { RetrofitClient.adminApi.getBooks("Bearer $token", size = 100) }
                val usersDeferred = async(Dispatchers.IO) { RetrofitClient.adminApi.getUsers("Bearer $token", size = 100) }
                val transactionsDeferred = async(Dispatchers.IO) { RetrofitClient.adminApi.getTransactions("Bearer $token", size = 100) }
                val notificationsDeferred = async(Dispatchers.IO) { RetrofitClient.adminApi.getNotifications("Bearer $token", size = 100) }

                val stats = runCatching { statsDeferred.await().data }.getOrNull()
                val books = runCatching { booksDeferred.await().data?.content.orEmpty() }.getOrDefault(emptyList())
                val users = runCatching { usersDeferred.await().data?.content.orEmpty() }.getOrDefault(emptyList())
                val transactions = runCatching { transactionsDeferred.await().data?.content.orEmpty() }.getOrDefault(emptyList())
                val notifications = runCatching { notificationsDeferred.await().data?.content.orEmpty() }.getOrDefault(emptyList())

                withContext(Dispatchers.Main) {
                    progressAdmin.visibility = View.GONE
                    adminDashboardScroll.visibility = View.VISIBLE
                    renderOverview(stats, books, users, transactions, notifications)
                }
            } catch (e: Exception) {
                showError("Failed to load admin overview: ${e.message}")
            }
        }
    }

    private fun renderOverview(
        stats: AdminPlatformStatsDTO?,
        books: List<BookDTO>,
        users: List<UserProfileDTO>,
        transactions: List<TransactionDTO>,
        notifications: List<NotificationDTO>
    ) {
        adminStatsContainer.removeAllViews()
        quickAccessContainer.removeAllViews()

        textAdminDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(Date())

        val activeTransactions = transactions.count { it.status.equals("Active", ignoreCase = true) }
        val pendingTransactions = transactions.count { it.status.equals("Pending", ignoreCase = true) }
        val unreadNotifications = notifications.count { !it.isRead }
        val suspendedUsers = users.count { it.accountStatus.equals("Suspended", ignoreCase = true) || it.accountStatus.equals("Banned", ignoreCase = true) }
        val unavailableBooks = books.count { it.status.equals("Unavailable", ignoreCase = true) }

        addStatCard("Total Users", users.size.toString(), "$suspendedUsers suspended/banned")
        addStatCard("Total Listings", books.size.toString(), "$unavailableBooks unavailable")
        addStatCard("Active Transactions", activeTransactions.toString(), "$pendingTransactions pending")
        addStatCard("Platform Revenue", String.format(Locale.getDefault(), "PHP %.0f", stats?.totalRevenue ?: 0.0), "${stats?.successfulPayments ?: 0} successful payments")
        addStatCard("Unread Notifications", unreadNotifications.toString(), "of ${notifications.size} total")

        addQuickAccessButton("Book Management") { openFragment(AdminBooksFragment()) }
        addQuickAccessButton("User Management") { openFragment(AdminUsersFragment()) }
        addQuickAccessButton("Transaction Management") { openFragment(AdminTransactionsFragment()) }
        addQuickAccessButton("Notification Logs") { openFragment(AdminNotificationsFragment()) }
    }

    private fun addStatCard(title: String, value: String, subtitle: String) {
        val cardView = layoutInflater.inflate(R.layout.item_admin_stat, adminStatsContainer, false)
        cardView.findViewById<TextView>(R.id.textStatTitle).text = title
        cardView.findViewById<TextView>(R.id.textStatValue).text = value
        cardView.findViewById<TextView>(R.id.textStatSubtitle).text = subtitle
        adminStatsContainer.addView(cardView)
    }

    private fun addQuickAccessButton(text: String, onClick: () -> Unit) {
        val button = Button(requireContext()).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { params ->
                params.bottomMargin = 24
            }
            setOnClickListener { onClick() }
        }
        quickAccessContainer.addView(button)
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressAdmin.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
