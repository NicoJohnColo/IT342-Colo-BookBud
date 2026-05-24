package com.example.bookbud.features.admin.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbud.R
import com.example.bookbud.features.admin.presentation.adapter.AdminNotificationAdapter
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.NotificationDTO
import com.example.bookbud.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminNotificationsFragment : Fragment() {
    private lateinit var progressNotifications: ProgressBar
    private lateinit var notificationsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var resultsCount: TextView
    private lateinit var searchNotifications: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterUnread: TextView
    private lateinit var filterRead: TextView
    private lateinit var btnBack: ImageButton

    private val allNotifications = mutableListOf<NotificationDTO>()
    private var activeFilter = "All"
    private var searchQuery = ""

    private val adapter = AdminNotificationAdapter { showNotificationDetails(it) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressNotifications = view.findViewById(R.id.progressNotifications)
        notificationsRecycler = view.findViewById(R.id.notificationsRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        resultsCount = view.findViewById(R.id.resultsCount)
        searchNotifications = view.findViewById(R.id.searchNotifications)
        filterAll = view.findViewById(R.id.filterAll)
        filterUnread = view.findViewById(R.id.filterUnread)
        filterRead = view.findViewById(R.id.filterRead)
        btnBack = view.findViewById(R.id.btnBack)

        notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        notificationsRecycler.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        searchNotifications.doAfterTextChanged {
            searchQuery = it?.toString().orEmpty()
            applyFilters()
        }

        filterAll.setOnClickListener { activeFilter = "All"; applyFilters() }
        filterUnread.setOnClickListener { activeFilter = "Unread"; applyFilters() }
        filterRead.setOnClickListener { activeFilter = "Read"; applyFilters() }

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    showError("Not authenticated")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.getNotifications("Bearer $token", size = 100)
                }

                withContext(Dispatchers.Main) {
                    progressNotifications.visibility = View.GONE
                    allNotifications.clear()
                    allNotifications.addAll(response.data?.content.orEmpty())
                    applyFilters()
                }
            } catch (e: Exception) {
                showError("Failed to load notifications: ${e.message}")
            }
        }
    }

    private fun applyFilters() {
        val filtered = allNotifications.filter { notification ->
            val matchesFilter = when (activeFilter) {
                "Unread" -> !notification.isRead
                "Read" -> notification.isRead
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() || listOf(notification.message, notification.userId)
                .any { it.contains(searchQuery, ignoreCase = true) }
            matchesFilter && matchesSearch
        }

        adapter.submitList(filtered)
        resultsCount.text = "${filtered.size} notifications"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        notificationsRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        emptyState.text = if (allNotifications.isEmpty()) "No notifications found" else "No notifications match your filters"
    }

    private fun showNotificationDetails(notification: NotificationDTO) {
        Toast.makeText(
            requireContext(),
            "${notification.message}\n${if (notification.isRead) "Read" else "Unread"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressNotifications.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyState.text = message
            notificationsRecycler.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
