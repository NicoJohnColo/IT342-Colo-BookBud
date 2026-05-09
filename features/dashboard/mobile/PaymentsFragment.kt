package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PaymentsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var contentScroll: ScrollView
    private lateinit var emptyState: TextView
    private lateinit var textEarningsTitle: TextView
    private lateinit var textEarningsSubtitle: TextView
    private lateinit var earningsContainer: LinearLayout
    private lateinit var paymentsContainer: LinearLayout
    private lateinit var tabsContainer: LinearLayout
    private var accessToken: String? = null

    private var selectedTab = "All"
    private var allPayments: List<PaymentDTO> = emptyList()
    private var earningsSummary: EarningsSummaryDTO? = null

    private val PAYMENT_TABS = listOf("All", "Pending", "Successful", "Failed")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressPayments)
        contentScroll = view.findViewById(R.id.contentScroll)
        emptyState = view.findViewById(R.id.emptyState)
        textEarningsTitle = view.findViewById(R.id.textEarningsTitle)
        textEarningsSubtitle = view.findViewById(R.id.textEarningsSubtitle)
        earningsContainer = view.findViewById(R.id.earningsContainer)
        paymentsContainer = view.findViewById(R.id.paymentsContainer)
        tabsContainer = view.findViewById(R.id.tabsContainer)

        // Back button - navigate to Profile
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        setupTabs()
        loadPaymentsData()
    }

    private fun setupTabs() {
        tabsContainer.removeAllViews()
        PAYMENT_TABS.forEach { tabName ->
            val tabView = createTab(tabName, tabName == selectedTab)
            tabView.setOnClickListener {
                selectedTab = tabName
                setupTabs()
                displayPayments()
            }
            tabsContainer.addView(tabView)
        }
    }

    private fun createTab(name: String, isActive: Boolean): TextView {
        return TextView(requireContext()).apply {
            text = name
            textSize = 13f
            setTypeface(null, if (isActive) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            setTextColor(ContextCompat.getColor(context,
                if (isActive) android.R.color.white else R.color.bb_text_muted))
            setPadding(24, 16, 24, 16)
            background = if (isActive) {
                ContextCompat.getDrawable(context, R.drawable.bg_tab_active)
            } else null
            setOnClickListener {
                selectedTab = name
                setupTabs()
                displayPayments()
            }
        }
    }

    private fun loadPaymentsData() {
        progressBar.visibility = View.VISIBLE
        contentScroll.visibility = View.GONE
        emptyState.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread

                // Load earnings summary
                val summaryResult = PaymentApiClient.getEarningsSummary(accessToken)
                earningsSummary = summaryResult.data

                // Load payments
                val paymentsResult = PaymentApiClient.getPaymentsReceived(accessToken, mapOf("size" to "100"))
                val paginated = paymentsResult.data
                allPayments = paginated?.content ?: emptyList()

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    contentScroll.visibility = View.VISIBLE
                    displayEarnings()
                    displayPayments()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    contentScroll.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun displayEarnings() {
        earningsContainer.removeAllViews()

        val summary = earningsSummary ?: EarningsSummaryDTO()
        val totalEarnings = summary.totalEarnings ?: 0.0
        val pendingCount = summary.pendingPayments ?: 0
        val successfulCount = summary.successfulPayments ?: 0

        // Total Earnings Card
        earningsContainer.addView(createEarningsCard(
            "Total Earnings",
            "PHP ${String.format("%.2f", totalEarnings)}",
            "From successful transactions",
            R.color.bb_success,
            "₱"
        ))

        // Pending Payments Card
        earningsContainer.addView(createEarningsCard(
            "Pending",
            "$pendingCount",
            "Awaiting confirmation",
            R.color.bb_warning,
            "⏳"
        ))

        // Confirmed Payments Card
        earningsContainer.addView(createEarningsCard(
            "Confirmed",
            "$successfulCount",
            "Successfully received",
            R.color.bb_success,
            "✓"
        ))
    }

    private fun createEarningsCard(
        label: String,
        value: String,
        description: String,
        colorRes: Int,
        icon: String
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 12
            }
            setPadding(20, 20, 20, 20)
            setBackgroundResource(R.drawable.bg_earnings_card)

            // Icon
            addView(TextView(context).apply {
                text = icon
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, colorRes))
            })

            // Value
            addView(TextView(context).apply {
                text = value
                textSize = 24f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 8 }
            })

            // Label
            addView(TextView(context).apply {
                text = label
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })

            // Description
            addView(TextView(context).apply {
                text = description
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 2 }
            })
        }
    }

    private fun displayPayments() {
        paymentsContainer.removeAllViews()

        val filtered = when (selectedTab.lowercase()) {
            "pending" -> allPayments.filter { it.paymentStatus?.lowercase() == "pending" }
            "successful" -> allPayments.filter { it.paymentStatus?.lowercase() == "paid" }
            "failed" -> allPayments.filter { it.paymentStatus?.lowercase() == "failed" }
            else -> allPayments
        }

        if (filtered.isEmpty()) {
            paymentsContainer.addView(TextView(requireContext()).apply {
                text = "No payments here"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
            })
            return
        }

        filtered.forEach { payment ->
            paymentsContainer.addView(createPaymentCard(payment))
        }
    }

    private fun createPaymentCard(payment: PaymentDTO): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            setPadding(20, 20, 20, 20)
            setBackgroundResource(R.drawable.bg_payment_card)
        }

        // Header row
        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Title section
        val titleSection = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        titleSection.addView(TextView(context).apply {
            text = payment.bookTitle ?: "Book Transaction"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.bb_text_dark))
        })

        titleSection.addView(TextView(context).apply {
            text = "From: ${payment.otherPartyName ?: "Unknown"}"
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })

        header.addView(titleSection)

        // Status badge
        val statusColor = when (payment.paymentStatus?.lowercase()) {
            "paid" -> R.color.bb_success
            "failed" -> R.color.bb_error
            else -> R.color.bb_warning
        }
        val statusText = when (payment.paymentStatus?.lowercase()) {
            "paid" -> "PAID"
            "failed" -> "FAILED"
            else -> "PENDING"
        }

        header.addView(TextView(context).apply {
            text = statusText
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, statusColor))
            setPadding(12, 6, 12, 6)
            background = ContextCompat.getDrawable(context, R.drawable.bg_status_badge)
        })

        card.addView(header)

        // Amount row
        card.addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }

            addView(TextView(context).apply {
                text = "Amount:"
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(context).apply {
                text = "PHP ${String.format("%.2f", payment.amount ?: 0.0)}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, R.color.bb_orange))
            })
        })

        // Payment method
        card.addView(TextView(context).apply {
            text = "Method: ${payment.paymentMethod ?: "Cash"}"
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, R.color.bb_text_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        })

        // Payment date if available
        payment.paymentDate?.let { date ->
            card.addView(TextView(context).apply {
                text = "Date: ${date.take(10)}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.bb_text_muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })
        }

        // Action button for pending payments
        if (payment.paymentStatus?.lowercase() == "pending" && payment.paymentId != null) {
            card.addView(android.widget.Button(context).apply {
                text = "✓ Mark as Received"
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setBackgroundResource(R.drawable.bg_button_primary)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
                setOnClickListener {
                    markPaymentAsReceived(payment.paymentId!!)
                }
            })
        }

        return card
    }

    private fun markPaymentAsReceived(paymentId: String) {
        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val result = PaymentApiClient.updatePaymentStatus(accessToken, paymentId, "Paid")

                requireActivity().runOnUiThread {
                    if (result.success) {
                        Toast.makeText(requireContext(), "Payment marked as received", Toast.LENGTH_SHORT).show()
                        loadPaymentsData()
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        loadPaymentsData()
    }
}
