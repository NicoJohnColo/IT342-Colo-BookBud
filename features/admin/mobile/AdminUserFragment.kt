package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminUserFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var usersRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchUsers: EditText
    private lateinit var resultsCount: TextView
    private var accessToken: String? = null
    private lateinit var userAdapter: AdminUserAdapter

    // Filter views
    private lateinit var filterAll: TextView
    private lateinit var filterActive: TextView
    private lateinit var filterSuspended: TextView
    private lateinit var filterBanned: TextView

    private var selectedFilter = "all"
    private var allUsers: List<UserProfileDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressUsers)
        usersRecycler = view.findViewById(R.id.usersRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        searchUsers = view.findViewById(R.id.searchUsers)
        resultsCount = view.findViewById(R.id.resultsCount)

        // Filter chips
        filterAll = view.findViewById(R.id.filterAll)
        filterActive = view.findViewById(R.id.filterActive)
        filterSuspended = view.findViewById(R.id.filterSuspended)
        filterBanned = view.findViewById(R.id.filterBanned)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        userAdapter = AdminUserAdapter(
            onSuspend = { user ->
                user.userId?.let { updateUserStatus(it, "Suspended") }
            },
            onBan = { user ->
                user.userId?.let { updateUserStatus(it, "Banned") }
            },
            onActivate = { user ->
                user.userId?.let { updateUserStatus(it, "Active") }
            }
        )
        usersRecycler.layoutManager = LinearLayoutManager(requireContext())
        usersRecycler.adapter = userAdapter

        setupFilters()
        loadUsers()
    }

    private fun setupFilters() {
        filterAll.setOnClickListener { setFilter("all", filterAll, listOf(filterActive, filterSuspended, filterBanned)) }
        filterActive.setOnClickListener { setFilter("active", filterActive, listOf(filterAll, filterSuspended, filterBanned)) }
        filterSuspended.setOnClickListener { setFilter("suspended", filterSuspended, listOf(filterAll, filterActive, filterBanned)) }
        filterBanned.setOnClickListener { setFilter("banned", filterBanned, listOf(filterAll, filterActive, filterSuspended)) }

        // Search listener
        searchUsers.setOnEditorActionListener { _, _, _ ->
            applyFilters()
            true
        }
    }

    private fun setFilter(status: String, selected: TextView, others: List<TextView>) {
        selectedFilter = status
        selected.setBackgroundResource(R.drawable.bg_badge_hot)
        selected.setTextColor(resources.getColor(android.R.color.white, null))
        others.forEach {
            it.setBackgroundResource(R.drawable.bg_badge_gray)
            it.setTextColor(resources.getColor(R.color.bb_text_dark, null))
        }
        applyFilters()
    }

    private fun applyFilters() {
        val query = searchUsers.text.toString().lowercase()

        val filtered = allUsers.filter { user ->
            val matchesSearch = query.isEmpty() ||
                    user.username?.lowercase()?.contains(query) == true ||
                    user.email?.lowercase()?.contains(query) == true

            val matchesStatus = when (selectedFilter) {
                "active" -> user.accountStatus?.lowercase() == "active"
                "suspended" -> user.accountStatus?.lowercase() == "suspended"
                "banned" -> user.accountStatus?.lowercase() == "banned"
                else -> true
            }

            matchesSearch && matchesStatus
        }

        userAdapter.updateData(filtered)
        resultsCount.text = "${filtered.size} users"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        usersRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        usersRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.getAllUsers(accessToken, mapOf("size" to "100"))
                allUsers = (result.data as? PaginatedResponse<UserProfileDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    resultsCount.text = "${allUsers.size} users"

                    if (allUsers.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                    } else {
                        usersRecycler.visibility = View.VISIBLE
                        applyFilters()
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

    private fun updateUserStatus(userId: String, status: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.updateUserStatus(accessToken, userId, status)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "User status updated to $status", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to update status", Toast.LENGTH_SHORT).show()
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

// Admin User Adapter
class AdminUserAdapter(
    private var users: List<UserProfileDTO> = emptyList(),
    private val onSuspend: (UserProfileDTO) -> Unit,
    private val onBan: (UserProfileDTO) -> Unit,
    private val onActivate: (UserProfileDTO) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView = itemView.findViewById<TextView>(R.id.userAvatar)
        private val usernameView = itemView.findViewById<TextView>(R.id.userUsername)
        private val emailView = itemView.findViewById<TextView>(R.id.userEmail)
        private val statusView = itemView.findViewById<TextView>(R.id.userStatus)
        private val joinedView = itemView.findViewById<TextView>(R.id.userJoined)
        private val ratingView = itemView.findViewById<TextView>(R.id.userRating)
        private val btnSuspend = itemView.findViewById<Button>(R.id.btnSuspend)
        private val btnBan = itemView.findViewById<Button>(R.id.btnBan)

        fun bind(user: UserProfileDTO) {
            val initials = user.username?.take(1)?.uppercase() ?: "U"
            avatarView.text = initials

            usernameView.text = user.username ?: "Unknown"
            emailView.text = user.email ?: "No email"
            statusView.text = user.accountStatus ?: "Active"
            statusView.background = when (user.accountStatus?.lowercase()) {
                "active" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_active)
                "suspended" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_pending)
                "banned" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_cancelled)
                else -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_active)
            }

            // Format joined date
            joinedView.text = "Joined: ${user.createdAt?.take(10) ?: "Unknown"}"

            // Rating
            val rating = user.rating?.toDoubleOrNull() ?: 0.0
            ratingView.text = "${String.format("%.1f", rating)} ★"

            // Update buttons based on status
            when (user.accountStatus?.lowercase()) {
                "active" -> {
                    btnSuspend.text = "Suspend"
                    btnSuspend.visibility = View.VISIBLE
                    btnBan.visibility = View.VISIBLE
                    btnSuspend.setOnClickListener { onSuspend(user) }
                    btnBan.setOnClickListener { onBan(user) }
                }
                "suspended" -> {
                    btnSuspend.text = "Activate"
                    btnSuspend.visibility = View.VISIBLE
                    btnBan.visibility = View.VISIBLE
                    btnSuspend.setOnClickListener { onActivate(user) }
                    btnBan.setOnClickListener { onBan(user) }
                }
                "banned" -> {
                    btnSuspend.text = "Activate"
                    btnSuspend.visibility = View.VISIBLE
                    btnBan.visibility = View.GONE
                    btnSuspend.setOnClickListener { onActivate(user) }
                }
                else -> {
                    btnSuspend.text = "Suspend"
                    btnSuspend.visibility = View.VISIBLE
                    btnBan.visibility = View.VISIBLE
                    btnSuspend.setOnClickListener { onSuspend(user) }
                    btnBan.setOnClickListener { onBan(user) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserProfileDTO>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
