package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class AdminDashboardFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var dashboardScroll: ScrollView
    private lateinit var statsContainer: LinearLayout
    private lateinit var quickAccessContainer: LinearLayout

    private var accessToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressAdmin)
        dashboardScroll = view.findViewById(R.id.adminDashboardScroll)
        statsContainer = view.findViewById(R.id.adminStatsContainer)
        quickAccessContainer = view.findViewById(R.id.quickAccessContainer)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)

        // Back button
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Logout button
        view.findViewById<Button>(R.id.btnAdminLogout)?.setOnClickListener {
            logout()
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE
        dashboardScroll.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread

                // Load all data for admin stats
                val booksResult = AdminApiClient.getAllBooks(accessToken, mapOf("size" to "1"))
                val booksTotal = (booksResult.data as? PaginatedResponse<*>)?.totalElements ?: 0

                val usersResult = AdminApiClient.getAllUsers(accessToken, mapOf("size" to "1"))
                val usersTotal = (usersResult.data as? PaginatedResponse<*>)?.totalElements ?: 0

                val transactionsResult = AdminApiClient.getAllTransactions(accessToken, mapOf("size" to "1"))
                val transactionsTotal = (transactionsResult.data as? PaginatedResponse<*>)?.totalElements ?: 0

                val notificationsResult = AdminApiClient.getAllNotifications(accessToken, mapOf("size" to "1"))
                val notificationsTotal = (notificationsResult.data as? PaginatedResponse<*>)?.totalElements ?: 0

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    dashboardScroll.visibility = View.VISIBLE

                    buildStatsCards(booksTotal.toInt(), usersTotal.toInt(), transactionsTotal.toInt(), notificationsTotal.toInt())
                    buildQuickAccessButtons()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    dashboardScroll.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error loading admin data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun buildStatsCards(books: Int, users: Int, transactions: Int, notifications: Int) {
        statsContainer.removeAllViews()

        // 4 stat cards: Total Listings, Total Users, Active Transactions, Notifications
        val cardWidth = (resources.displayMetrics.widthPixels * 0.22).toInt()

        statsContainer.addView(createStatCard("📚", "$books", "Listings", cardWidth))
        statsContainer.addView(createStatCard("👥", "$users", "Users", cardWidth))
        statsContainer.addView(createStatCard("⇄", "$transactions", "Transactions", cardWidth))
        statsContainer.addView(createStatCard("🔔", "$notifications", "Alerts", cardWidth))
    }

    private fun createStatCard(icon: String, value: String, label: String, width: Int): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 12
            }
            setBackgroundResource(R.drawable.bg_stat_card)
            setPadding(16, 16, 16, 16)

            // Icon
            addView(TextView(requireContext()).apply {
                text = icon
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
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

    private fun buildQuickAccessButtons() {
        quickAccessContainer.removeAllViews()

        // Book Management
        quickAccessContainer.addView(createQuickAccessButton(
            icon = "📚",
            title = "Book Management",
            subtitle = "All books • Set unavailable • Delete",
            onClick = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminBookFragment())
                    .addToBackStack(null)
                    .commit()
            }
        ))

        // User Management
        quickAccessContainer.addView(createQuickAccessButton(
            icon = "👥",
            title = "User Management",
            subtitle = "All users • Suspend • Ban",
            onClick = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminUserFragment())
                    .addToBackStack(null)
                    .commit()
            }
        ))

        // Transactions
        quickAccessContainer.addView(createQuickAccessButton(
            icon = "⇄",
            title = "Transactions",
            subtitle = "All transactions • Cancel",
            onClick = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminTransactionFragment())
                    .addToBackStack(null)
                    .commit()
            }
        ))

        // Notifications
        quickAccessContainer.addView(createQuickAccessButton(
            icon = "🔔",
            title = "Notification Logs",
            subtitle = "All notifications",
            onClick = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AdminNotificationFragment())
                    .addToBackStack(null)
                    .commit()
            }
        ))
    }

    private fun createQuickAccessButton(icon: String, title: String, subtitle: String, onClick: () -> Unit): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            setBackgroundResource(R.drawable.bg_book_card)
            setPadding(16, 16, 16, 16)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            // Icon
            addView(TextView(requireContext()).apply {
                text = icon
                textSize = 24f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
            })

            // Text content
            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(TextView(requireContext()).apply {
                    this.text = title
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })

                addView(TextView(requireContext()).apply {
                    this.text = subtitle
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 4
                    }
                })
            })

            // Arrow
            addView(TextView(requireContext()).apply {
                text = "›"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
