package com.example.bookbud.features.admin.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookbud.R
import com.example.bookbud.shared.models.BookDTO
import com.example.bookbud.shared.models.NotificationDTO
import com.example.bookbud.shared.models.TransactionDTO
import com.example.bookbud.shared.models.UserProfileDTO
import java.util.Locale

private fun shortId(value: String?, length: Int = 8): String {
    if (value.isNullOrBlank()) return "N/A"
    return if (value.length <= length) value else value.substring(0, length)
}

private fun displayDate(value: String?): String {
    if (value.isNullOrBlank()) return "N/A"
    return value.substringBefore('T').replace('-', '/')
}

private fun displayDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "N/A"
    return value.replace('T', ' ').substring(0, minOf(16, value.length))
}

private fun displayMoney(value: Double?): String {
    return if (value == null) "N/A" else String.format(Locale.getDefault(), "PHP %.2f", value)
}

private fun firstLetter(value: String?): String {
    return value?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

class AdminBookAdapter(
    private val onViewDetails: (BookDTO) -> Unit,
    private val onSetUnavailable: (BookDTO) -> Unit,
    private val onDelete: (BookDTO) -> Unit
) : RecyclerView.Adapter<AdminBookAdapter.BookViewHolder>() {
    private val items = mutableListOf<BookDTO>()

    fun submitList(newItems: List<BookDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: TextView = itemView.findViewById(R.id.bookAvatar)
        private val title: TextView = itemView.findViewById(R.id.bookTitle)
        private val author: TextView = itemView.findViewById(R.id.bookAuthor)
        private val genre: TextView = itemView.findViewById(R.id.bookGenre)
        private val owner: TextView = itemView.findViewById(R.id.bookWishlistIcon)
        private val transactionType: TextView = itemView.findViewById(R.id.bookTransactionType)
        private val price: TextView = itemView.findViewById(R.id.bookPrice)
        private val btnUnavailable: Button = itemView.findViewById(R.id.btnUnavailable)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(book: BookDTO) {
            avatar.text = firstLetter(book.title)
            title.text = book.title
            author.text = book.author
            genre.text = listOfNotNull(book.genre, book.condition).joinToString(" • ").ifBlank { "Unknown" }
            owner.text = book.ownerUsername ?: shortId(book.ownerId)
            transactionType.text = listOfNotNull(book.transactionType, book.status).joinToString(" • ").ifBlank { "Unknown" }
            price.text = displayMoney(book.priceSale ?: book.priceRent)

            btnUnavailable.isEnabled = !book.status.equals("Unavailable", ignoreCase = true)
            btnUnavailable.text = if (book.status.equals("Unavailable", ignoreCase = true)) "Unavailable" else "Set Unavailable"

            itemView.setOnClickListener { onViewDetails(book) }
            btnUnavailable.setOnClickListener { onSetUnavailable(book) }
            btnDelete.setOnClickListener { onDelete(book) }
        }
    }
}

class AdminUserAdapter(
    private val onViewDetails: (UserProfileDTO) -> Unit,
    private val onStatusChange: (UserProfileDTO, String) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {
    private val items = mutableListOf<UserProfileDTO>()

    fun submitList(newItems: List<UserProfileDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: TextView = itemView.findViewById(R.id.userAvatar)
        private val username: TextView = itemView.findViewById(R.id.userUsername)
        private val email: TextView = itemView.findViewById(R.id.userEmail)
        private val status: TextView = itemView.findViewById(R.id.userStatus)
        private val rating: TextView = itemView.findViewById(R.id.userRating)
        private val joined: TextView = itemView.findViewById(R.id.userJoined)
        private val btnSuspend: Button = itemView.findViewById(R.id.btnSuspend)
        private val btnBan: Button = itemView.findViewById(R.id.btnBan)

        fun bind(user: UserProfileDTO) {
            val accountStatus = user.accountStatus?.takeIf { it.isNotBlank() } ?: "Active"
            val role = user.role?.takeIf { it.isNotBlank() } ?: "USER"

            avatar.text = firstLetter(user.username)
            username.text = user.username
            email.text = user.email ?: "N/A"
            status.text = accountStatus
            rating.text = user.rating?.let { String.format(Locale.getDefault(), "%.1f ★", it) } ?: "N/A"
            joined.text = "Role: $role • Joined: ${displayDate(user.createdAt)}"

            btnSuspend.text = if (accountStatus.equals("Suspended", ignoreCase = true)) "Restore" else "Suspend"
            btnBan.text = if (accountStatus.equals("Banned", ignoreCase = true)) "Banned" else "Ban"
            btnBan.isEnabled = !accountStatus.equals("Banned", ignoreCase = true)

            itemView.setOnClickListener { onViewDetails(user) }
            btnSuspend.setOnClickListener {
                onStatusChange(user, if (accountStatus.equals("Suspended", ignoreCase = true)) "Active" else "Suspended")
            }
            btnBan.setOnClickListener { onStatusChange(user, "Banned") }
        }
    }
}

class AdminTransactionAdapter(
    private val onViewDetails: (TransactionDTO) -> Unit,
    private val onCancel: (TransactionDTO) -> Unit
) : RecyclerView.Adapter<AdminTransactionAdapter.TransactionViewHolder>() {
    private val items = mutableListOf<TransactionDTO>()

    fun submitList(newItems: List<TransactionDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txnId: TextView = itemView.findViewById(R.id.txnId)
        private val txnStatus: TextView = itemView.findViewById(R.id.txnStatus)
        private val txnBookTitle: TextView = itemView.findViewById(R.id.txnBookTitle)
        private val txnUsers: TextView = itemView.findViewById(R.id.txnUsers)
        private val txnDates: TextView = itemView.findViewById(R.id.txnDates)
        private val txnAmount: TextView = itemView.findViewById(R.id.txnAmount)
        private val btnCancel: Button = itemView.findViewById(R.id.btnCancelTransaction)

        fun bind(txn: TransactionDTO) {
            val status = txn.status?.takeIf { it.isNotBlank() } ?: "Unknown"
            txnId.text = shortId(txn.transactionId)
            txnStatus.text = status
            txnBookTitle.text = txn.bookTitle ?: "Unknown Book"
            txnUsers.text = "${txn.ownerUsername ?: shortId(txn.ownerId, 6)} → ${txn.renterUsername ?: shortId(txn.userId, 6)}"
            txnDates.text = when {
                !txn.startDate.isNullOrBlank() && !txn.endDate.isNullOrBlank() -> "${displayDate(txn.startDate)} → ${displayDate(txn.endDate)}"
                !txn.createdAt.isNullOrBlank() -> displayDateTime(txn.createdAt)
                else -> "N/A"
            }
            txnAmount.text = displayMoney(txn.amount)

            val cancellable = !status.equals("Completed", ignoreCase = true) && !status.equals("Cancelled", ignoreCase = true)
            btnCancel.visibility = if (cancellable) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onViewDetails(txn) }
            btnCancel.setOnClickListener { onCancel(txn) }
        }
    }
}

class AdminNotificationAdapter(
    private val onViewDetails: (NotificationDTO) -> Unit
) : RecyclerView.Adapter<AdminNotificationAdapter.NotificationViewHolder>() {
    private val items = mutableListOf<NotificationDTO>()

    fun submitList(newItems: List<NotificationDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.notifIcon)
        private val unreadDot: View = itemView.findViewById(R.id.notifUnreadDot)
        private val title: TextView = itemView.findViewById(R.id.notifTitle)
        private val time: TextView = itemView.findViewById(R.id.notifTime)
        private val message: TextView = itemView.findViewById(R.id.notifMessage)
        private val subtitle: TextView = itemView.findViewById(R.id.notifSubtitle)
        private val badge: TextView = itemView.findViewById(R.id.notifStatusBadge)

        fun bind(notification: NotificationDTO) {
            icon.text = firstLetter(notification.userId)
            unreadDot.visibility = if (notification.isRead) View.GONE else View.VISIBLE
            title.text = if (notification.userId.isBlank()) "System" else "User ${shortId(notification.userId, 6)}"
            time.text = displayDateTime(notification.createdAt)
            message.text = notification.message
            subtitle.text = "Notification"
            badge.text = if (notification.isRead) "Read" else "Unread"

            itemView.setOnClickListener { onViewDetails(notification) }
        }
    }
}
