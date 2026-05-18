package com.example.bookbud.features.transactions.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.TransactionApiClient
import com.example.bookbud.shared.network.PaymentApiClient
import kotlinx.coroutines.*

class TransactionsFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        loadTransactions(view)
    }

    private fun loadTransactions(view: View) {
        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: return@launch
            val transactions = TransactionApiClient.getMyTransactions(token)
            
            withContext(Dispatchers.Main) {
                val container = view.findViewById<LinearLayout>(R.id.transactionsContainer)
                container?.removeAllViews()
                
                transactions.forEach { txn ->
                    val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction, container, false)
                    
                    itemView.findViewById<TextView>(R.id.txnBookTitle)?.text = txn.bookTitle ?: "Book"
                    
                    val statusBadge = itemView.findViewById<TextView>(R.id.txnStatusBadge)
                    statusBadge?.text = txn.status ?: "Pending"
                    
                    itemView.findViewById<TextView>(R.id.txnOwner)?.text = "Owner: ${txn.ownerUsername ?: "User"}"
                    itemView.findViewById<TextView>(R.id.txnRenter)?.text = "Renter: ${txn.renterUsername ?: "User"}"
                    
                    val dateStr = if (txn.endDate != null) "${txn.startDate} to ${txn.endDate}" else txn.startDate
                    itemView.findViewById<TextView>(R.id.txnDates)?.text = dateStr
                    
                    itemView.findViewById<TextView>(R.id.txnAmount)?.text = String.format("PHP %.2f", txn.amount)
                    
                    val paymentStatusTv = itemView.findViewById<TextView>(R.id.txnPaymentStatus)
                    val pStatus = txn.paymentStatus ?: "Pending"
                    paymentStatusTv?.text = pStatus
                    
                    if (pStatus.uppercase() == "PAID" || pStatus.uppercase() == "SUCCESSFUL" || pStatus.uppercase() == "COMPLETED") {
                        paymentStatusTv?.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                    } else {
                        paymentStatusTv?.setTextColor(resources.getColor(R.color.bb_orange, null))
                    }

                    // Action buttons
                    val actionsContainer = itemView.findViewById<View>(R.id.txnActionsContainer)
                    val btn1 = itemView.findViewById<TextView>(R.id.txnActionButton1)
                    val btn2 = itemView.findViewById<TextView>(R.id.txnActionButton2)
                    
                    if (pStatus.uppercase() == "PENDING" && txn.status?.uppercase() == "PENDING") {
                        actionsContainer?.visibility = View.VISIBLE
                        btn1?.text = "Settle Payment"
                        btn1?.setOnClickListener {
                            lifecycleScope.launch {
                                val success = PaymentApiClient.confirmPayment(token, txn.transactionId)
                                if (success) {
                                    Toast.makeText(requireContext(), "Payment settled successfully!", Toast.LENGTH_SHORT).show()
                                    loadTransactions(view)
                                } else {
                                    Toast.makeText(requireContext(), "Failed to settle payment.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        btn2?.text = "Cancel"
                        btn2?.setOnClickListener {
                            lifecycleScope.launch {
                                TransactionApiClient.updateStatus(token, txn.transactionId, "Cancelled")
                                Toast.makeText(requireContext(), "Transaction cancelled", Toast.LENGTH_SHORT).show()
                                loadTransactions(view)
                            }
                        }
                    } else {
                        actionsContainer?.visibility = View.GONE
                    }

                    container?.addView(itemView)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
