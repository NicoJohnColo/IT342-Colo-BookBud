package edu.cit.colo.bookbud

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        bottomNav = findViewById(R.id.adminBottomNavigation)

        if (savedInstanceState == null) {
            showFragment(AdminDashboardFragment(), "admin_dashboard")
            bottomNav.selectedItemId = R.id.admin_nav_dashboard
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin_nav_dashboard -> showFragment(AdminDashboardFragment(), "admin_dashboard")
                R.id.admin_nav_books -> showFragment(AdminBookFragment(), "admin_books")
                R.id.admin_nav_users -> showFragment(AdminUserFragment(), "admin_users")
                R.id.admin_nav_transactions -> showFragment(AdminTransactionFragment(), "admin_transactions")
                R.id.admin_nav_notifications -> showFragment(AdminNotificationFragment(), "admin_notifications")
                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (bottomNav.selectedItemId != R.id.admin_nav_dashboard) {
            bottomNav.selectedItemId = R.id.admin_nav_dashboard
        } else {
            super.onBackPressed()
        }
    }

    private fun showFragment(fragment: Fragment, tag: String): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment, tag)
            .commitNow()
        return true
    }
}