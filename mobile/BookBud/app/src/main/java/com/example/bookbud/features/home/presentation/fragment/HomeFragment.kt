package com.example.bookbud.features.home.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.features.auth.presentation.activity.LoginActivity
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.BookApiClient
import com.example.bookbud.shared.network.TransactionApiClient
import com.example.bookbud.shared.network.PaymentApiClient
import kotlinx.coroutines.*

class HomeFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDashboardData(view)
    }

    private fun loadDashboardData(view: View) {
        val progressHome = view.findViewById<ProgressBar>(R.id.progressHome)
        val homeScroll = view.findViewById<ScrollView>(R.id.homeScroll)
        val textUsername = view.findViewById<TextView>(R.id.textUsername)
        val kpiStatsContainer = view.findViewById<LinearLayout>(R.id.kpiStatsContainer)
        val featuredBooksContainer = view.findViewById<LinearLayout>(R.id.featuredBooksContainer)
        val transactionsContainer = view.findViewById<LinearLayout>(R.id.transactionsContainer)
        val emptyBooksText = view.findViewById<TextView>(R.id.emptyBooksText)
        val emptyTransactionsText = view.findViewById<TextView>(R.id.emptyTransactionsText)

        progressHome?.visibility = View.VISIBLE
        homeScroll?.visibility = View.GONE

        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken()
            if (token == null) {
                withContext(Dispatchers.Main) {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    activity?.finish()
                }
                return@launch
            }
            
            val userId = TokenManager.getUserId() ?: ""
            val username = TokenManager.getUsername() ?: "Reader"

            // Parallel fetch using async
            val booksDeferred = async(Dispatchers.IO) { BookApiClient.getAllBooks() }
            val transactionsDeferred = async(Dispatchers.IO) { TransactionApiClient.getMyTransactions(token) }
            val earningsDeferred = async(Dispatchers.IO) { PaymentApiClient.getEarningsSummary(token) }

            val books = try { booksDeferred.await() } catch (e: Exception) { null }
            val transactions = try { transactionsDeferred.await() } catch (e: Exception) { emptyList() }
            val earnings = try { earningsDeferred.await() } catch (e: Exception) { null }

            withContext(Dispatchers.Main) {
                progressHome?.visibility = View.GONE
                homeScroll?.visibility = View.VISIBLE

                textUsername?.text = username

                val myListingsCount = books?.count { it.ownerId == userId } ?: 0
                val activeTransactionsCount = transactions.count { it.status?.lowercase() == "active" }
                val totalEarnings = earnings?.totalEarnings ?: 0.0

                // 1. Populate KPI Stats Cards Dynamically
                kpiStatsContainer?.removeAllViews()
                val primaryColor = resources.getColor(R.color.bb_orange, null)
                val darkColor = resources.getColor(R.color.bb_text_dark, null)
                
                kpiStatsContainer?.addView(createKpiCard("My Listings", myListingsCount.toString(), primaryColor))
                kpiStatsContainer?.addView(createKpiCard("Active Orders", activeTransactionsCount.toString(), darkColor))
                kpiStatsContainer?.addView(createKpiCard("Total Earnings", String.format("PHP %.2f", totalEarnings), primaryColor))

                // 2. Populate Featured Books
                featuredBooksContainer?.removeAllViews()
                if (books.isNullOrEmpty()) {
                    emptyBooksText?.visibility = View.VISIBLE
                } else {
                    emptyBooksText?.visibility = View.GONE
                    books.take(5).forEach { book ->
                        val bookView = LayoutInflater.from(requireContext()).inflate(R.layout.item_book, featuredBooksContainer, false)
                        bookView.findViewById<TextView>(R.id.bookTitle)?.text = book.title
                        bookView.findViewById<TextView>(R.id.bookAuthor)?.text = book.author
                        bookView.findViewById<TextView>(R.id.bookPrice)?.text = String.format("PHP %.2f", book.priceSale ?: book.priceRent ?: 0.0)
                        
                        // Set standard margin
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 24, 0)
                        }
                        bookView.layoutParams = params
                        featuredBooksContainer?.addView(bookView)
                    }
                }

                // 3. Populate Recent Transactions
                transactionsContainer?.removeAllViews()
                if (transactions.isEmpty()) {
                    emptyTransactionsText?.visibility = View.VISIBLE
                } else {
                    emptyTransactionsText?.visibility = View.GONE
                    transactions.take(3).forEach { txn ->
                        val txnView = LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction, transactionsContainer, false)
                        txnView.findViewById<TextView>(R.id.txnBookTitle)?.text = txn.bookTitle ?: "Book"
                        txnView.findViewById<TextView>(R.id.txnStatusBadge)?.text = txn.status ?: "Pending"
                        txnView.findViewById<TextView>(R.id.txnAmount)?.text = String.format("PHP %.2f", txn.amount)
                        transactionsContainer?.addView(txnView)
                    }
                }
            }
        }
    }

    private fun createKpiCard(title: String, value: String, valueColor: Int): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 36, 48, 36)
            background = resources.getDrawable(R.drawable.bg_stat_card, null)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 24, 0)
            }
            layoutParams = params
        }

        val titleTv = TextView(requireContext()).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(resources.getColor(R.color.bb_text_muted, null))
        }

        val valueTv = TextView(requireContext()).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(valueColor)
            setPadding(0, 8, 0, 0)
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
