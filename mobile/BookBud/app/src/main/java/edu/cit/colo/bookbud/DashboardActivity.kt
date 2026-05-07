package edu.cit.colo.bookbud

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var currentFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_new)

        bottomNav = findViewById(R.id.bottomNavigation)

        // Load user data from SharedPreferences
        val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)

        // Save userId if not already saved
        if (userId == null && prefs.contains("username")) {
            prefs.edit().putString("user_id", "user_unknown").apply()
        }

        // Show home fragment by default
        if (savedInstanceState == null) {
            showFragment(HomeFragment(), "home")
            bottomNav.selectedItemId = R.id.nav_home
        } else {
            currentFragmentTag = savedInstanceState.getString("current_fragment_tag")
        }

        // Bottom Navigation (max 5 items supported)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment(), "home")
                R.id.nav_explore -> showFragment(BooksFragment(), "explore")
                R.id.nav_listings -> showFragment(ListingsFragment(), "listings")
                R.id.nav_notifications -> showFragment(NotificationsFragment(), "notifications")
                R.id.nav_profile -> showFragment(ProfileFragment(), "profile")
                else -> false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_fragment_tag", currentFragmentTag)
    }

    private fun showFragment(fragment: Fragment, tag: String): Boolean {
        if (currentFragmentTag == tag) return true

        currentFragmentTag = tag
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment, tag)
            .commitNow()
        return true
    }
}
