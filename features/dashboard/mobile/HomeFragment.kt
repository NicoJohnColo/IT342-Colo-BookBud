package edu.cit.colo.bookbud

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var kpiStatsContainer: LinearLayout
    private lateinit var featuredBooksContainer: LinearLayout
    private lateinit var transactionsContainer: LinearLayout
    private lateinit var homeScroll: ScrollView
    private lateinit var emptyBooksText: TextView
    private lateinit var emptyTransactionsText: TextView
    private lateinit var textUsername: TextView
    private lateinit var textSeeAllFeatured: TextView
    private lateinit var textSeeAllListed: TextView
    private var accessToken: String? = null
    private var userId: String? = null
    private var username: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressHome)
        kpiStatsContainer = view.findViewById(R.id.kpiStatsContainer)
        featuredBooksContainer = view.findViewById(R.id.featuredBooksContainer)
        transactionsContainer = view.findViewById(R.id.transactionsContainer)
        homeScroll = view.findViewById(R.id.homeScroll)
        emptyBooksText = view.findViewById(R.id.emptyBooksText)
        emptyTransactionsText = view.findViewById(R.id.emptyTransactionsText)
        textUsername = view.findViewById(R.id.textUsername)
        textSeeAllFeatured = view.findViewById(R.id.textSeeAllFeatured)
        textSeeAllListed = view.findViewById(R.id.textSeeAllListed)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)
        username = prefs.getString("username", "Reader")

        textUsername.text = username ?: "Reader"

        loadDashboardData()
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE
        homeScroll.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                
                // Load books
                val booksResult = BookApiClient.getAllBooks(mapOf("size" to "100"))
                val booksList = (booksResult.data as? PaginatedResponse<BookDTO>)?.content ?: emptyList()

                // Load transactions
                val transResult = TransactionApiClient.getMyTransactions(accessToken, mapOf("size" to "100"))
                val transList = (transResult.data as? PaginatedResponse<TransactionDTO>)?.content ?: emptyList()

                // Load notifications
                val notifResult = NotificationApiClient.getMyNotifications(accessToken)
                val notifList = (notifResult.data as? List<NotificationDTO>) ?: emptyList()

                // Load user profile
                val userResult = userId?.let { UserApiClient.getUserProfile(accessToken, it) }
                val userProfile = (userResult?.data as? UserProfileDTO)

                // Load earnings summary
                val earningsResult = PaymentApiClient.getEarningsSummary(accessToken)
                val earnings = earningsResult.data

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    homeScroll.visibility = View.VISIBLE
                    
                    // Update UI with data
                    updateUI(booksList, transList, notifList, userProfile, earnings)
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    homeScroll.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun updateUI(
        books: List<BookDTO>,
        transactions: List<TransactionDTO>,
        notifications: List<NotificationDTO>,
        userProfile: UserProfileDTO?,
        earnings: EarningsSummaryDTO? = null
    ) {
        // Clear existing views
        kpiStatsContainer.removeAllViews()
        featuredBooksContainer.removeAllViews()
        transactionsContainer.removeAllViews()

        // 1. Build KPI Stats Cards
        buildKpiStats(books, transactions, notifications, earnings)

        // 2. Build Featured Books Section
        if (books.isEmpty()) {
            emptyBooksText.visibility = View.VISIBLE
        } else {
            emptyBooksText.visibility = View.GONE
            buildFeaturedBooks(books.take(5))
        }

        // 3. Build Recently Listed Section (show recent books)
        val recentBooks = books.sortedByDescending { it.createdAt }.take(3)
        if (recentBooks.isEmpty()) {
            emptyTransactionsText.visibility = View.VISIBLE
        } else {
            emptyTransactionsText.visibility = View.GONE
            buildRecentlyListed(recentBooks)
        }
    }

    private fun buildKpiStats(
        books: List<BookDTO>,
        transactions: List<TransactionDTO>,
        notifications: List<NotificationDTO>,
        earnings: EarningsSummaryDTO? = null
    ) {
        val activeTxn = transactions.count { it.status?.lowercase() == "active" }
        val pendingTxn = transactions.count { it.status?.lowercase() == "pending" }
        val totalEarnings = earnings?.totalEarnings ?: 0.0
        val wishlistCount = 0 // TODO: Implement wishlist

        // 5 stat cards in horizontal scroll: Active, Pending, Listings, Wishlist, Earnings
        val cardWidth = (resources.displayMetrics.widthPixels * 0.20).toInt()

        kpiStatsContainer.addView(createStatCard("⇄", "$activeTxn", "Active", cardWidth))
        kpiStatsContainer.addView(createStatCard("⏳", "$pendingTxn", "Pending", cardWidth))
        kpiStatsContainer.addView(createStatCard("📋", "${books.size}", "Listings", cardWidth))
        kpiStatsContainer.addView(createStatCard("♡", "$wishlistCount", "Wishlist", cardWidth))
        kpiStatsContainer.addView(createStatCard("₱", "${String.format("%.0f", totalEarnings)}", "Earnings", cardWidth))
    }

    private fun createStatCard(icon: String, value: String, label: String, width: Int, onClick: (() -> Unit)? = null): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 12
            }
            setBackgroundResource(R.drawable.bg_stat_card)
            setPadding(16, 16, 16, 16)
            if (onClick != null) {
                isClickable = true
                isFocusable = true
                setOnClickListener { onClick() }
            }

            // Icon
            addView(TextView(requireContext()).apply {
                text = icon
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            // Value
            addView(TextView(requireContext()).apply {
                text = value
                textSize = 22f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            })

            // Label
            addView(TextView(requireContext()).apply {
                text = label
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 2
                }
            })
        }
    }

    private fun createKpiCard(value: String, label: String, colorHex: String): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(16, 16, 16, 16)
            elevation = 2f

            // Parse color hex to int
            val color = try {
                android.graphics.Color.parseColor(colorHex)
            } catch (e: Exception) {
                ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            }

            // Value TextView
            addView(TextView(requireContext()).apply {
                text = value
                textSize = 28f
                setTextColor(color)
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            // Label TextView
            addView(TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
            })
        }
    }

    private fun buildFeaturedBooks(books: List<BookDTO>) {
        books.take(5).forEach { book ->
            featuredBooksContainer.addView(createFeaturedBookCard(book))
        }
    }

    private fun createFeaturedBookCard(book: BookDTO): View {
        val cardWidth = (resources.displayMetrics.widthPixels * 0.35).toInt()

        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 16
            }
            setBackgroundResource(R.drawable.bg_book_card)
            setPadding(0, 0, 0, 12)

            // Book Cover Placeholder
            val coverLayout = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (cardWidth * 1.4).toInt()
                )
                setBackgroundResource(R.drawable.bg_book_cover_placeholder)

                // Book initial or title
                addView(TextView(requireContext()).apply {
                    text = book.title?.take(1)?.uppercase() ?: "?"
                    textSize = 32f
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                })
            }
            addView(coverLayout)

            // Hot badge (if applicable)
            if (book.transactionType?.lowercase() == "both") {
                addView(TextView(requireContext()).apply {
                    text = "Hot"
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    setBackgroundResource(R.drawable.bg_badge_hot)
                    setPadding(8, 4, 8, 4)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8
                        marginStart = 12
                    }
                })
            }

            // Book title
            addView(TextView(requireContext()).apply {
                text = book.title ?: "Unknown"
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                    marginStart = 12
                    marginEnd = 12
                }
            })

            // Author
            addView(TextView(requireContext()).apply {
                text = book.author ?: "Unknown"
                textSize = 11f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                    marginStart = 12
                }
            })

            // Price
            val priceText = when {
                book.priceSale != null && book.priceSale > 0 -> "₱${book.priceSale.toInt()}"
                book.priceRent != null && book.priceRent > 0 -> "₱${book.priceRent.toInt()}/day"
                else -> "Price on request"
            }
            addView(TextView(requireContext()).apply {
                text = priceText
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.bb_orange))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 6
                    marginStart = 12
                }
            })
        }
    }

    private fun buildRecentlyListed(books: List<BookDTO>) {
        books.forEach { book ->
            transactionsContainer.addView(createRecentlyListedCard(book))
        }
    }

    private fun createRecentlyListedCard(book: BookDTO): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setBackgroundResource(R.drawable.bg_book_card)
            setPadding(14, 14, 14, 14)

            // Book Cover Icon
            val iconFrame = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(44, 44)
                setBackgroundResource(R.drawable.bg_recently_listed_icon)

                addView(TextView(requireContext()).apply {
                    text = book.title?.take(1)?.uppercase() ?: "?"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                })
            }
            addView(iconFrame)

            // Book Info
            val infoLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 12
                }

                // Title
                addView(TextView(requireContext()).apply {
                    text = book.title ?: "Unknown"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })

                // Author
                addView(TextView(requireContext()).apply {
                    text = book.author ?: "Unknown"
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 2
                    }
                })

                // Badges row
                val badgesLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 6
                    }

                    // Transaction type badge
                    addView(TextView(requireContext()).apply {
                        text = book.transactionType?.replaceFirstChar { it.uppercase() } ?: "N/A"
                        textSize = 10f
                        setTextColor(ContextCompat.getColor(context, R.color.bb_orange_dark))
                        setBackgroundResource(R.drawable.bg_badge_hot)
                        setPadding(8, 4, 8, 4)
                    })

                    // Condition badge
                    addView(TextView(requireContext()).apply {
                        text = book.condition ?: "N/A"
                        textSize = 10f
                        setTextColor(ContextCompat.getColor(context, R.color.bb_badge_gray_text))
                        setBackgroundResource(R.drawable.bg_badge_gray)
                        setPadding(8, 4, 8, 4)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            marginStart = 6
                        }
                    })
                }
                addView(badgesLayout)
            }
            addView(infoLayout)

            // Price & Location
            val priceLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END

                val priceText = when {
                    book.priceSale != null && book.priceSale > 0 -> "₱${book.priceSale.toInt()}"
                    book.priceRent != null && book.priceRent > 0 -> "₱${book.priceRent.toInt()}"
                    else -> "N/A"
                }

                addView(TextView(requireContext()).apply {
                    text = priceText
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.bb_orange))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    gravity = Gravity.END
                })

                addView(TextView(requireContext()).apply {
                    text = "Cebu City" // TODO: Get actual location
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    gravity = Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 2
                    }
                })
            }
            addView(priceLayout)
        }
    }
}
