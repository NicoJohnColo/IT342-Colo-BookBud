package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminNotificationFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var notificationsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchNotifications: EditText
    private lateinit var resultsCount: TextView
    private var accessToken: String? = null
    private lateinit var notifAdapter: AdminNotificationAdapter

    // Filter views
    private lateinit var filterAll: TextView
    private lateinit var filterUnread: TextView
    private lateinit var filterRead: TextView

    private var selectedFilter = "all"
    private var allNotifications: List<NotificationDTO> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressNotifications)
        notificationsRecycler = view.findViewById(R.id.notificationsRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        searchNotifications = view.findViewById(R.id.searchNotifications)
        resultsCount = view.findViewById(R.id.resultsCount)

        // Filter chips
        filterAll = view.findViewById(R.id.filterAll)
        filterUnread = view.findViewById(R.id.filterUnread)
        filterRead = view.findViewById(R.id.filterRead)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        notifAdapter = AdminNotificationAdapter()
        notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        notificationsRecycler.adapter = notifAdapter

        setupFilters()
        loadNotifications()
    }

    private fun setupFilters() {
        filterAll.setOnClickListener { setFilter("all", filterAll, listOf(filterUnread, filterRead)) }
        filterUnread.setOnClickListener { setFilter("unread", filterUnread, listOf(filterAll, filterRead)) }
        filterRead.setOnClickListener { setFilter("read", filterRead, listOf(filterAll, filterUnread)) }

        // Search listener
        searchNotifications.setOnEditorActionListener { _, _, _ ->
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
        val query = searchNotifications.text.toString().lowercase()

        var filtered = allNotifications.filter { notif ->
            val matchesSearch = query.isEmpty() ||
                    notif.message?.lowercase()?.contains(query) == true ||
                    notif.userId?.lowercase()?.contains(query) == true

            val matchesStatus = when (selectedFilter) {
                "unread" -> !notif.isRead
                "read" -> notif.isRead
                else -> true
            }

            matchesSearch && matchesStatus
        }

        notifAdapter.updateData(filtered)
        resultsCount.text = "${filtered.size} notifications"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        notificationsRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        notificationsRecycler.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.getAllNotifications(accessToken, mapOf("size" to "100"))
                allNotifications = (result.data as? PaginatedResponse<NotificationDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    applyFilters()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}

// Admin Notification Adapter
class AdminNotificationAdapter(
    private var notifications: List<NotificationDTO> = emptyList()
) : RecyclerView.Adapter<AdminNotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView = itemView.findViewById<TextView>(R.id.notifIcon)
        private val titleView = itemView.findViewById<TextView>(R.id.notifTitle)
        private val timeView = itemView.findViewById<TextView>(R.id.notifTime)
        private val messageView = itemView.findViewById<TextView>(R.id.notifMessage)
        private val subtitleView = itemView.findViewById<TextView>(R.id.notifSubtitle)
        private val unreadDot = itemView.findViewById<View>(R.id.notifUnreadDot)
        private val statusBadge = itemView.findViewById<TextView>(R.id.notifStatusBadge)

        fun bind(notif: NotificationDTO) {
            // Use first letter of user ID as icon
            val initials = notif.userId?.take(1)?.uppercase() ?: "N"
            iconView.text = initials

            titleView.text = notif.userId ?: "System"
            timeView.text = notif.createdAt?.take(10) ?: "Just now"
            messageView.text = notif.message ?: "No message"
            subtitleView.text = notif.type ?: "Notification"

            // Unread indicator
            unreadDot.visibility = if (notif.isRead) View.GONE else View.VISIBLE

            // Status badge
            statusBadge.text = if (notif.isRead) "Read" else "Unread"
            statusBadge.background = if (notif.isRead) {
                itemView.context.getDrawable(R.drawable.bg_badge_gray)
            } else {
                itemView.context.getDrawable(R.drawable.bg_status_pending)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    fun updateData(newNotifications: List<NotificationDTO>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
