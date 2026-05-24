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
import com.example.bookbud.features.admin.presentation.adapter.AdminTransactionAdapter
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.models.TransactionDTO
import com.example.bookbud.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminTransactionsFragment : Fragment() {
    private lateinit var progressTransactions: ProgressBar
    private lateinit var transactionsRecycler: RecyclerView
    private lateinit var emptyState: TextView
    private lateinit var resultsCount: TextView
    private lateinit var searchTransactions: EditText
    private lateinit var tabAll: TextView
    private lateinit var tabPending: TextView
    private lateinit var tabActive: TextView
    private lateinit var tabCompleted: TextView
    private lateinit var tabCancelled: TextView
    private lateinit var btnBack: ImageButton

    private val allTransactions = mutableListOf<TransactionDTO>()
    private var activeFilter = "All"
    private var searchQuery = ""

    private val adapter = AdminTransactionAdapter(
        onViewDetails = { showTransactionDetails(it) },
        onCancel = { cancelTransaction(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressTransactions = view.findViewById(R.id.progressTransactions)
        transactionsRecycler = view.findViewById(R.id.transactionsRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        resultsCount = view.findViewById(R.id.resultsCount)
        searchTransactions = view.findViewById(R.id.searchTransactions)
        tabAll = view.findViewById(R.id.tabAll)
        tabPending = view.findViewById(R.id.tabPending)
        tabActive = view.findViewById(R.id.tabActive)
        tabCompleted = view.findViewById(R.id.tabCompleted)
        tabCancelled = view.findViewById(R.id.tabCancelled)
        btnBack = view.findViewById(R.id.btnBack)

        transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionsRecycler.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        searchTransactions.doAfterTextChanged {
            searchQuery = it?.toString().orEmpty()
            applyFilters()
        }

        tabAll.setOnClickListener { activeFilter = "All"; applyFilters() }
        tabPending.setOnClickListener { activeFilter = "Pending"; applyFilters() }
        tabActive.setOnClickListener { activeFilter = "Active"; applyFilters() }
        tabCompleted.setOnClickListener { activeFilter = "Completed"; applyFilters() }
        tabCancelled.setOnClickListener { activeFilter = "Cancelled"; applyFilters() }

        loadTransactions()
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token.isNullOrBlank()) {
                    showError("Not authenticated")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.getTransactions("Bearer $token", size = 100)
                }

                withContext(Dispatchers.Main) {
                    progressTransactions.visibility = View.GONE
                    allTransactions.clear()
                    allTransactions.addAll(response.data?.content.orEmpty())
                    applyFilters()
                }
            } catch (e: Exception) {
                showError("Failed to load transactions: ${e.message}")
            }
        }
    }

    private fun applyFilters() {
        val filtered = allTransactions.filter { txn ->
            val status = txn.status?.takeIf { it.isNotBlank() } ?: "Unknown"
            val matchesFilter = activeFilter == "All" || status.equals(activeFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || listOf(txn.transactionId, txn.bookTitle, txn.ownerUsername, txn.renterUsername)
                .any { it?.contains(searchQuery, ignoreCase = true) == true }
            matchesFilter && matchesSearch
        }

        adapter.submitList(filtered)
        resultsCount.text = "${filtered.size} transactions"
        emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        transactionsRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        emptyState.text = if (allTransactions.isEmpty()) "No transactions found" else "No transactions match your filters"
    }

    private fun showTransactionDetails(txn: TransactionDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Transaction ${txn.transactionId.take(8)}")
            .setMessage(
                buildString {
                    appendLine("Book: ${txn.bookTitle ?: "N/A"}")
                    appendLine("Owner: ${txn.ownerUsername ?: txn.ownerId}")
                    appendLine("Renter/Buyer: ${txn.renterUsername ?: txn.userId}")
                    appendLine("Status: ${txn.status ?: "N/A"}")
                    appendLine("Amount: ${txn.amount}")
                    appendLine("Created: ${txn.createdAt ?: "N/A"}")
                    appendLine("Start: ${txn.startDate ?: "N/A"}")
                    appendLine("End: ${txn.endDate ?: "N/A"}")
                }
            )
            .setPositiveButton("Cancel Transaction") { _, _ -> cancelTransaction(txn) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun cancelTransaction(txn: TransactionDTO) {
        if (txn.status.equals("Completed", ignoreCase = true) || txn.status.equals("Cancelled", ignoreCase = true)) {
            Toast.makeText(requireContext(), "Transaction already closed", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val token = TokenManager.getAccessToken() ?: return@launch
                withContext(Dispatchers.IO) {
                    RetrofitClient.adminApi.cancelTransaction(txn.transactionId, "Bearer $token")
                }
                Toast.makeText(requireContext(), "Transaction cancelled", Toast.LENGTH_SHORT).show()
                loadTransactions()
            } catch (e: Exception) {
                showError("Failed to cancel transaction: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            progressTransactions.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyState.text = message
            transactionsRecycler.visibility = View.GONE
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
