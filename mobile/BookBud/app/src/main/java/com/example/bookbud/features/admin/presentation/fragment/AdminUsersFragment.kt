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
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbud.R
import com.example.bookbud.features.admin.presentation.adapter.AdminUserAdapter
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.UserProfileDTO
import com.example.bookbud.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminUsersFragment : Fragment() {
    private lateinit var progressUsers: ProgressBar
    private lateinit var usersRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var resultsCount: TextView
    private lateinit var searchUsers: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterActive: TextView
    private lateinit var filterSuspended: TextView
    private lateinit var filterBanned: TextView
    private lateinit var btnBack: ImageButton

    private val allUsers = mutableListOf<UserProfileDTO>()
    private var activeFilter = "All"
    private var searchQuery = ""

    private val adapter = AdminUserAdapter(
        onViewDetails = { showUserDetails(it) },
        onStatusChange = { user, status -> updateUserStatus(user, status) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressUsers = view.findViewById(R.id.progressUsers)
        usersRecycler = view.findViewById(R.id.usersRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        resultsCount = view.findViewById(R.id.resultsCount)
        searchUsers = view.findViewById(R.id.searchUsers)
        filterAll = view.findViewById(R.id.filterAll)
        filterActive = view.findViewById(R.id.filterActive)
        filterSuspended = view.findViewById(R.id.filterSuspended)
        filterBanned = view.findViewById(R.id.filterBanned)
        btnBack = view.findViewById(R.id.btnBack)

        usersRecycler.layoutManager = LinearLayoutManager(requireContext())
        usersRecycler.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        searchUsers.doAfterTextChanged {
            searchQuery = it?.toString().orEmpty()
            applyFilters()
        }

        filterAll.setOnClickListener { activeFilter = "All"; applyFilters() }
        filterActive.setOnClickListener { activeFilter = "Active"; applyFilters() }
        filterSuspended.setOnClickListener { activeFilter = "Suspended"; applyFilters() }
        filterBanned.setOnClickListener { activeFilter = "Banned"; applyFilters() }

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    showError("Not authenticated")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.getUsers("Bearer $token", size = 100)
                }

                withContext(Dispatchers.Main) {
                    progressUsers.visibility = View.GONE
                    allUsers.clear()
                    allUsers.addAll(response.data?.content.orEmpty())
                    applyFilters()
                }
            } catch (e: Exception) {
                showError("Failed to load users: ${e.message}")
            }
        }
    }

    private fun applyFilters() {
        val filtered = allUsers.filter { user ->
            val status = user.accountStatus?.takeIf { it.isNotBlank() } ?: "Active"
            val matchesFilter = activeFilter == "All" || status.equals(activeFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || listOf(user.username, user.email, user.role, status)
                .any { it?.contains(searchQuery, ignoreCase = true) == true }
            matchesFilter && matchesSearch
        }

        adapter.submitList(filtered)
        resultsCount.text = "${filtered.size} users"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        usersRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        emptyState.text = if (allUsers.isEmpty()) "No users found" else "No users match your filters"
    }

    private fun showUserDetails(user: UserProfileDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle(user.username)
            .setMessage(
                buildString {
                    appendLine("User ID: ${user.userId}")
                    appendLine("Email: ${user.email ?: "N/A"}")
                    appendLine("Role: ${user.role ?: "USER"}")
                    appendLine("Status: ${user.accountStatus ?: "Active"}")
                    appendLine("Rating: ${user.rating ?: 0.0}")
                    appendLine("Joined: ${user.createdAt ?: "N/A"}")
                    appendLine("Facebook: ${user.facebookUrl ?: "N/A"}")
                    appendLine("Messenger: ${user.messenger ?: "N/A"}")
                    appendLine("Mobile: ${user.mobileNumber ?: "N/A"}")
                }
            )
            .setPositiveButton("Suspend") { _, _ -> updateUserStatus(user, "Suspended") }
            .setNeutralButton("Ban") { _, _ -> updateUserStatus(user, "Banned") }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun updateUserStatus(user: UserProfileDTO, status: String) {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken() ?: return@launch
                withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.updateUserStatus(user.userId, "Bearer $token", status)
                }
                Toast.makeText(requireContext(), "User status updated", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                showError("Failed to update user: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressUsers.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyState.text = message
            usersRecycler.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
