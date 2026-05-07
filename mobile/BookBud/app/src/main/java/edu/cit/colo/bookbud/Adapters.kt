package edu.cit.colo.bookbud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<TransactionDTO> = emptyList(),
    private val onActionClick: (TransactionDTO, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView = itemView.findViewById<TextView>(R.id.txnBookTitle)
        private val statusBadge = itemView.findViewById<TextView>(R.id.txnStatusBadge)
        private val ownerView = itemView.findViewById<TextView>(R.id.txnOwner)
        private val renterView = itemView.findViewById<TextView>(R.id.txnRenter)
        private val datesView = itemView.findViewById<TextView>(R.id.txnDates)
        private val amountView = itemView.findViewById<TextView>(R.id.txnAmount)
        private val paymentView = itemView.findViewById<TextView>(R.id.txnPaymentStatus)
        private val actionsContainer = itemView.findViewById<ViewGroup>(R.id.txnActionsContainer)

        fun bind(txn: TransactionDTO) {
            titleView.text = txn.bookTitle ?: "Unknown"
            statusBadge.text = txn.status ?: "Pending"
            statusBadge.background = when (txn.status?.lowercase()) {
                "active" -> itemView.context.getDrawable(R.drawable.bg_status_active)
                "completed" -> itemView.context.getDrawable(R.drawable.bg_status_completed)
                "cancelled" -> itemView.context.getDrawable(R.drawable.bg_status_cancelled)
                else -> itemView.context.getDrawable(R.drawable.bg_status_pending)
            }

            ownerView.text = "Owner: ${txn.ownerUsername ?: txn.ownerId}"
            renterView.text = "Renter: ${txn.renterUsername ?: txn.userId}"
            datesView.text = "${txn.startDate} → ${txn.endDate}"
            amountView.text = "PHP ${txn.amount?.toInt()}"
            paymentView.text = txn.paymentStatus ?: "Pending"

            // Set up action buttons based on transaction state
            actionsContainer.removeAllViews()
            if (txn.status?.lowercase() == "pending") {
                val btn1 = Button(itemView.context).apply {
                    text = "Approve"
                    setOnClickListener { onActionClick(txn, 0) }
                }
                actionsContainer.addView(btn1)
            }

            val canRate = txn.status?.lowercase() == "completed" && when (txn.userRole?.lowercase()) {
                "owner" -> txn.ownerRated != true
                "renter" -> txn.renterRated != true
                else -> false
            }

            if (canRate) {
                val btnRate = Button(itemView.context).apply {
                    text = "Rate User"
                    setOnClickListener { onActionClick(txn, 2) }
                }
                actionsContainer.addView(btnRate)
            }

            val btn2 = Button(itemView.context).apply {
                text = "Details"
                setOnClickListener { onActionClick(txn, 1) }
            }
            actionsContainer.addView(btn2)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<TransactionDTO>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}

class BookAdapter(
    private var books: List<BookDTO> = emptyList(),
    private val onBookClick: (BookDTO) -> Unit = { },
    private val onEditClick: ((BookDTO) -> Unit)? = null,
    private val onDeleteClick: ((BookDTO) -> Unit)? = null
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView = itemView.findViewById<TextView>(R.id.bookAvatar)
        private val titleView = itemView.findViewById<TextView>(R.id.bookTitle)
        private val authorView = itemView.findViewById<TextView>(R.id.bookAuthor)
        private val genreView = itemView.findViewById<TextView>(R.id.bookGenre)
        private val priceView = itemView.findViewById<TextView>(R.id.bookPrice)
        private val typeView = itemView.findViewById<TextView>(R.id.bookTransactionType)
        private val wishlistIcon = itemView.findViewById<TextView>(R.id.bookWishlistIcon)

        fun bind(book: BookDTO) {
            // Avatar with first letter
            val initials = book.title?.firstOrNull()?.uppercase() ?: "B"
            avatarView.text = initials

            titleView.text = book.title ?: "Unknown"
            authorView.text = book.author ?: "Unknown"
            genreView.text = "${book.genre} • ${book.condition}"
            priceView.text = "₱${(book.priceSale ?: book.priceRent ?: 0.0).toInt()}"
            typeView.text = when {
                book.transactionType?.lowercase() == "both" -> "Rent & Sale"
                else -> book.transactionType ?: "For Sale"
            }

            // Show edit/delete buttons if callbacks are provided
            if (onEditClick != null || onDeleteClick != null) {
                wishlistIcon.visibility = View.GONE
                // Long press for edit, double tap for delete - or we can add buttons
                itemView.setOnLongClickListener {
                    showBookActions(book)
                    true
                }
            }

            itemView.setOnClickListener { onBookClick(book) }
        }

        private fun showBookActions(book: BookDTO) {
            val options = mutableListOf<String>()
            if (onEditClick != null) options.add("Edit")
            if (onDeleteClick != null) options.add("Delete")

            if (options.isEmpty()) return

            androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                .setTitle(book.title ?: "Book Actions")
                .setItems(options.toTypedArray()) { _, which ->
                    when (options[which]) {
                        "Edit" -> onEditClick?.invoke(book)
                        "Delete" -> onDeleteClick?.invoke(book)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    fun updateData(newBooks: List<BookDTO>) {
        books = newBooks
        notifyDataSetChanged()
    }
}

class NotificationAdapter(
    private var notifications: List<NotificationDTO> = emptyList(),
    private val onMarkUnread: (NotificationDTO) -> Unit = { },
    private val onDelete: (NotificationDTO) -> Unit = { }
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView = itemView.findViewById<TextView>(R.id.notifIcon)
        private val titleView = itemView.findViewById<TextView>(R.id.notifTitle)
        private val timeView = itemView.findViewById<TextView>(R.id.notifTime)
        private val messageView = itemView.findViewById<TextView>(R.id.notifMessage)
        private val subtitleView = itemView.findViewById<TextView>(R.id.notifSubtitle)
        private val unreadDot = itemView.findViewById<View>(R.id.notifUnreadDot)
        private val btnUnread = itemView.findViewById<Button>(R.id.notifActionUnread)
        private val btnDelete = itemView.findViewById<Button>(R.id.notifActionDelete)

        fun bind(notif: NotificationDTO) {
            titleView.text = notif.userId ?: "Notification"
            timeView.text = notif.createdAt?.take(10) ?: "Just now"
            messageView.text = notif.message ?: "No message"
            subtitleView.text = notif.type ?: "Update"

            unreadDot.visibility = if (notif.isRead) View.GONE else View.VISIBLE
            btnUnread.text = if (notif.isRead) "Mark Unread" else "Mark Read"

            btnUnread.setOnClickListener { onMarkUnread(notif) }
            btnDelete.setOnClickListener { onDelete(notif) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
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

class WishlistAdapter(
    private var items: List<WishlistItemDTO> = emptyList(),
    private val onRemove: (WishlistItemDTO) -> Unit = { },
    private val onClick: (WishlistItemDTO) -> Unit = { }
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView = itemView.findViewById<TextView>(R.id.bookAvatar)
        private val titleView = itemView.findViewById<TextView>(R.id.bookTitle)
        private val authorView = itemView.findViewById<TextView>(R.id.bookAuthor)
        private val genreView = itemView.findViewById<TextView>(R.id.bookGenre)
        private val priceView = itemView.findViewById<TextView>(R.id.bookPrice)
        private val typeView = itemView.findViewById<TextView>(R.id.bookTransactionType)
        private val removeBtn = itemView.findViewById<TextView>(R.id.bookWishlistIcon)

        fun bind(item: WishlistItemDTO) {
            val book = item.book
            avatarView.text = book?.title?.take(1)?.uppercase() ?: "?"
            titleView.text = book?.title ?: "Unknown"
            authorView.text = book?.author ?: "Unknown Author"
            genreView.text = book?.genre ?: "General"
            typeView.text = book?.transactionType ?: "For Rent"

            priceView.text = when (book?.transactionType?.lowercase()) {
                "sale" -> "PHP ${book.priceSale?.toInt()}"
                "rent" -> "PHP ${book.priceRent?.toInt()}/day"
                "both" -> "PHP ${book.priceSale?.toInt()} / PHP ${book.priceRent?.toInt()}/day"
                else -> "PHP -"
            }

            removeBtn.text = "♥"
            removeBtn.setOnClickListener { onRemove(item) }
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<WishlistItemDTO>) {
        items = newItems
        notifyDataSetChanged()
    }
}
