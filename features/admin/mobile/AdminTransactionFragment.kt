package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminTransactionFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var searchTransactions: EditText
    private lateinit var resultsCount: TextView
    private var accessToken: String? = null
    private lateinit var txnAdapter: AdminTransactionAdapter

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
        return inflater.inflate(R.layout.fragment_admin_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressTransactions)
        transactionsRecycler = view.findViewById(R.id.transactionsRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        searchTransactions = view.findViewById(R.id.searchTransactions)
        resultsCount = view.findViewById(R.id.resultsCount)

        // Tabs
        tabAll = view.findViewById(R.id.tabAll)
        tabPending = view.findViewById(R.id.tabPending)
        tabActive = view.findViewById(R.id.tabActive)
        tabCompleted = view.findViewById(R.id.tabCompleted)
        tabCancelled = view.findViewById(R.id.tabCancelled)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        txnAdapter = AdminTransactionAdapter(
            onCancel = { txn ->
                txn.transactionId?.let { cancelTransaction(it) }
            },
            onViewDetails = { txn ->
                showTransactionDetails(txn)
            }
        )
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

        // Search listener
        searchTransactions.setOnEditorActionListener { _, _, _ ->
            applyTabFilter()
            true
        }
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
        val query = searchTransactions.text.toString().lowercase()

        var filtered = if (selectedTab == "all") {
            allTransactions
        } else {
            allTransactions.filter { it.status?.lowercase() == selectedTab }
        }

        // Apply search filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { txn ->
                txn.bookTitle?.lowercase()?.contains(query) == true ||
                        txn.renterUsername?.lowercase()?.contains(query) == true ||
                        txn.ownerUsername?.lowercase()?.contains(query) == true
            }
        }

        txnAdapter.updateData(filtered)
        resultsCount.text = "${filtered.size} transactions"
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
                val result = AdminApiClient.getAllTransactions(accessToken, mapOf("size" to "100"))
                allTransactions = (result.data as? PaginatedResponse<TransactionDTO>)?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    applyTabFilter()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun cancelTransaction(transactionId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Transaction")
            .setMessage("Are you sure you want to cancel this transaction? This action cannot be undone.")
            .setPositiveButton("Cancel Transaction") { _, _ ->
                performCancelTransaction(transactionId)
            }
            .setNegativeButton("Keep Active", null)
            .show()
    }

    private fun performCancelTransaction(transactionId: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = AdminApiClient.cancelTransaction(accessToken, transactionId)

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Transaction cancelled successfully!", Toast.LENGTH_SHORT).show()
                        loadTransactions()
                    } else {
                        Toast.makeText(requireContext(), result.message ?: "Failed to cancel transaction", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showTransactionDetails(txn: TransactionDTO) {
        val message = buildString {
            appendLine("Transaction ID: ${txn.transactionId}")
            appendLine("Book: ${txn.bookTitle}")
            appendLine("Owner: ${txn.ownerUsername} (${txn.ownerId})")
            appendLine("Renter: ${txn.renterUsername} (${txn.userId})")
            appendLine()
            appendLine("Status: ${txn.status}")
            appendLine("Payment Status: ${txn.paymentStatus}")
            appendLine("Payment Method: ${txn.paymentMethod}")
            appendLine()
            appendLine("Amount: ₱${txn.amount?.toInt()}")
            appendLine("Dates: ${txn.startDate} → ${txn.endDate}")
            appendLine()
            appendLine("Created: ${txn.createdAt}")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Transaction Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}

// Admin Transaction Adapter
class AdminTransactionAdapter(
    private var transactions: List<TransactionDTO> = emptyList(),
    private val onCancel: (TransactionDTO) -> Unit,
    private val onViewDetails: (TransactionDTO) -> Unit
) : RecyclerView.Adapter<AdminTransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txnIdView = itemView.findViewById<TextView>(R.id.txnId)
        private val bookTitleView = itemView.findViewById<TextView>(R.id.txnBookTitle)
        private val usersView = itemView.findViewById<TextView>(R.id.txnUsers)
        private val statusView = itemView.findViewById<TextView>(R.id.txnStatus)
        private val datesView = itemView.findViewById<TextView>(R.id.txnDates)
        private val amountView = itemView.findViewById<TextView>(R.id.txnAmount)
        private val btnCancel = itemView.findViewById<Button>(R.id.btnCancelTransaction)

        fun bind(txn: TransactionDTO) {
            txnIdView.text = txn.transactionId?.take(8) ?: "TXN-???"
            bookTitleView.text = txn.bookTitle ?: "Unknown Book"
            usersView.text = "@${txn.renterUsername} → @${txn.ownerUsername}"

            statusView.text = txn.status ?: "Pending"
            statusView.background = when (txn.status?.lowercase()) {
                "active" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_active)
                "completed" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_completed)
                "cancelled" -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_cancelled)
                else -> ContextCompat.getDrawable(itemView.context, R.drawable.bg_status_pending)
            }

            datesView.text = "${txn.startDate} → ${txn.endDate}"
            amountView.text = "₱${txn.amount?.toInt() ?: 0}"

            // Show cancel button only for pending/active transactions
            if (txn.status?.lowercase() == "pending" || txn.status?.lowercase() == "active") {
                btnCancel.visibility = View.VISIBLE
                btnCancel.setOnClickListener { onCancel(txn) }
            } else {
                btnCancel.visibility = View.GONE
            }

            // Click to view details
            itemView.setOnClickListener { onViewDetails(txn) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_transaction, parent, false)
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
