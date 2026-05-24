package com.example.bookbud.features.admin.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bookbud.R
import com.example.bookbud.features.admin.presentation.fragment.AdminDashboardFragment
import com.example.bookbud.features.admin.presentation.fragment.AdminBooksFragment
import com.example.bookbud.features.admin.presentation.fragment.AdminUsersFragment
import com.example.bookbud.features.admin.presentation.fragment.AdminTransactionsFragment
import com.example.bookbud.features.admin.presentation.fragment.AdminNotificationsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.adminBottomNavigation)

        // Load dashboard fragment by default
        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin_nav_dashboard -> {
                    loadFragment(AdminDashboardFragment())
                    true
                }
                R.id.admin_nav_books -> {
                    loadFragment(AdminBooksFragment())
                    true
                }
                R.id.admin_nav_users -> {
                    loadFragment(AdminUsersFragment())
                    true
                }
                R.id.admin_nav_transactions -> {
                    loadFragment(AdminTransactionsFragment())
                    true
                }
                R.id.admin_nav_notifications -> {
                    loadFragment(AdminNotificationsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
