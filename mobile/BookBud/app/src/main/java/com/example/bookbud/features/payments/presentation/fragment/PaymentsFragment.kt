package com.example.bookbud.features.payments.presentation.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.PaymentApiClient
import com.example.bookbud.shared.network.TransactionApiClient
import kotlinx.coroutines.*

class PaymentsFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_payments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPayments(view)
    }

    private fun loadPayments(view: View) {
        val progressPayments = view.findViewById<ProgressBar>(R.id.progressPayments)
        val contentScroll = view.findViewById<ScrollView>(R.id.contentScroll)
        val earningsContainer = view.findViewById<LinearLayout>(R.id.earningsContainer)
        val paymentsContainer = view.findViewById<LinearLayout>(R.id.paymentsContainer)
        val emptyState = view.findViewById<TextView>(R.id.emptyState)

        progressPayments?.visibility = View.VISIBLE
        contentScroll?.visibility = View.GONE
        emptyState?.visibility = View.GONE

        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: return@launch
            
            val earningsDeferred = async(Dispatchers.IO) { PaymentApiClient.getEarningsSummary(token) }
            val paymentsDeferred = async(Dispatchers.IO) { PaymentApiClient.getPaymentsReceived(token) }
            val transactionsDeferred = async(Dispatchers.IO) { TransactionApiClient.getMyTransactions(token) }

            val earnings = try { earningsDeferred.await() } catch (e: Exception) { null }
            val payments = try { paymentsDeferred.await() } catch (e: Exception) { emptyList() }
            val transactions = try { transactionsDeferred.await() } catch (e: Exception) { emptyList() }

            withContext(Dispatchers.Main) {
                progressPayments?.visibility = View.GONE
                contentScroll?.visibility = View.VISIBLE

                // 1. Populate Earnings KPI cards dynamically (use server summary but prefer local-filtered totals)
                earningsContainer?.removeAllViews()
                val orangeColor = resources.getColor(R.color.bb_orange, null)
                val darkColor = resources.getColor(R.color.bb_text_dark, null)

                // Build transaction lookup to ensure we only count payments belonging to this user
                val txMap = transactions.associateBy { it.transactionId }
                val currentUserId = TokenManager.getUserId() ?: ""

                // Filter payments to those for which we have a transaction and which involve the current user
                val paymentsFiltered = payments.filter { p ->
                    val tx = txMap[p.transactionId]
                    tx != null && (tx.ownerId == currentUserId || tx.userId == currentUserId)
                }

                // Deduplicate payments by transactionId, preferring Paid > Pending > Failed
                fun rankStatus(s: String?): Int {
                    return when (s?.lowercase()) {
                        "paid", "successful", "completed" -> 3
                        "pending" -> 2
                        "failed" -> 1
                        else -> 0
                    }
                }

                val deduped = mutableMapOf<String, Any>()
                val bestPayments = mutableMapOf<String, Any>()
                val paymentByTxn = mutableMapOf<String, Any>()
                val selectedPayments = mutableMapOf<String, kotlin.Any>()
                // We'll keep a map of transactionId -> PaymentDTO (as Any, cast later)
                val chosen = mutableMapOf<String, Any>()
                for (p in paymentsFiltered) {
                    val existing = chosen[p.transactionId]
                    if (existing == null) {
                        chosen[p.transactionId] = p
                    } else {
                        val existingStatus = try { (existing as com.example.bookbud.shared.models.PaymentDTO).paymentStatus } catch (e: Exception) { null }
                        val newRank = rankStatus(p.paymentStatus)
                        val oldRank = rankStatus(existingStatus)
                        if (newRank > oldRank) chosen[p.transactionId] = p
                    }
                }

                val filteredPayments = chosen.values.map { it as com.example.bookbud.shared.models.PaymentDTO }

                // Compute local earnings based on filtered payments and transactions
                val totalEarningsLocal = filteredPayments.filter { fp ->
                    val tx = txMap[fp.transactionId]
                    tx != null && tx.ownerId == currentUserId && fp.paymentStatus.equals("Paid", true)
                }.sumOf { it.amount }

                val pendingLocal = filteredPayments.count { fp ->
                    val tx = txMap[fp.transactionId]
                    tx != null && tx.ownerId == currentUserId && fp.paymentStatus.equals("Pending", true)
                }

                val successLocal = filteredPayments.count { fp ->
                    val tx = txMap[fp.transactionId]
                    tx != null && tx.ownerId == currentUserId && fp.paymentStatus.equals("Paid", true)
                }

                val totalEarnings = if (totalEarningsLocal > 0.0) totalEarningsLocal else (earnings?.totalEarnings ?: 0.0)
                val pending = if (pendingLocal > 0) pendingLocal else (earnings?.pendingPayments ?: 0)
                val success = if (successLocal > 0) successLocal else (earnings?.successfulPayments ?: 0)

                earningsContainer?.addView(createStatCard("Total Earnings", String.format("PHP %.2f", totalEarnings), orangeColor))
                earningsContainer?.addView(createStatCard("Pending", pending.toString(), darkColor))
                earningsContainer?.addView(createStatCard("Successful", success.toString(), orangeColor))

                // 2. Populate Payments List
                paymentsContainer?.removeAllViews()
                if (filteredPayments.isEmpty()) {
                    val emptyMsg = TextView(requireContext()).apply {
                        text = "No payments received yet"
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                        setPadding(0, 48, 0, 0)
                        gravity = android.view.Gravity.CENTER
                        setTextColor(resources.getColor(R.color.bb_text_muted, null))
                    }
                    paymentsContainer?.addView(emptyMsg)
                } else {
                    // Show chosen/deduped payments (sorted by paymentDate desc if available)
                    val sorted = filteredPayments.sortedByDescending { it.paymentDate ?: "" }
                    sorted.forEach { payment ->
                        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_payment, paymentsContainer, false)
                        itemView.findViewById<TextView>(R.id.paymentAmount)?.text = String.format("PHP %.2f", payment.amount)

                        val statusTv = itemView.findViewById<TextView>(R.id.paymentStatus)
                        val status = payment.paymentStatus ?: "PENDING"
                        statusTv?.text = status

                        if (status.equals("Paid", true) || status.equals("Successful", true) || status.equals("Completed", true)) {
                            statusTv?.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                        } else {
                            statusTv?.setTextColor(resources.getColor(R.color.bb_orange, null))
                        }

                        // Payment method formatting: prefer same label as web UI
                        val methodTv = itemView.findViewById<TextView>(R.id.paymentMethod)
                        val rawMethod = payment.paymentMethod ?: "Cash"
                        val methodText = when {
                            rawMethod.contains("stripe", true) -> "Stripe (Card)"
                            rawMethod.contains("bank", true) -> "Bank Transfer"
                            rawMethod.contains("gcash", true) -> "GCash"
                            else -> rawMethod.replace("_", " ")
                        }
                        methodTv?.text = methodText

                        val dateTv = itemView.findViewById<TextView>(R.id.paymentDate)
                        dateTv?.text = payment.paymentDate ?: ""

                        paymentsContainer?.addView(itemView)
                    }
                }
            }
        }
    }

    private fun createStatCard(title: String, value: String, valueColor: Int): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 24, 36, 24)
            background = resources.getDrawable(R.drawable.bg_stat_card, null)
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 16, 0)
            }
            layoutParams = params
        }

        val titleTv = TextView(requireContext()).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTextColor(resources.getColor(R.color.bb_text_muted, null))
            maxLines = 1
        }

        val valueTv = TextView(requireContext()).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(valueColor)
            setPadding(0, 4, 0, 0)
            maxLines = 1
        }

        card.addView(titleTv)
        card.addView(valueTv)
        return card
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
