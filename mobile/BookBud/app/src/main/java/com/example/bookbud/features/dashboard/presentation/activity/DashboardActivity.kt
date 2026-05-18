package com.example.bookbud.features.dashboard.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bookbud.R
import com.example.bookbud.features.home.presentation.fragment.HomeFragment
import com.example.bookbud.features.books.presentation.fragment.BooksFragment
import com.example.bookbud.features.books.presentation.fragment.ListingsFragment
import com.example.bookbud.features.transactions.presentation.fragment.TransactionsFragment
import com.example.bookbud.features.profile.presentation.fragment.ProfileFragment
import com.example.bookbud.features.notifications.presentation.fragment.NotificationsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_explore -> BooksFragment()
                R.id.nav_listings -> ListingsFragment()
                R.id.nav_notifications -> NotificationsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
