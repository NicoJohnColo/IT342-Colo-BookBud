package com.example.bookbud

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookbud.features.auth.presentation.activity.GetStartedActivity
import com.example.bookbud.features.admin.presentation.activity.AdminDashboardActivity
import com.example.bookbud.shared.storage.PreferencesManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Navigate to appropriate screen after 1.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 1500)
    }
    
    private fun navigateToNextScreen() {
        try {
            val isLoggedIn = try {
                com.example.bookbud.shared.auth.TokenManager.isLoggedIn()
            } catch (e: Exception) {
                Log.e("SplashActivity", "Error checking token", e)
                false // Default to not logged in if error occurs
            }
            
            val intent = if (isLoggedIn) {
                // User is logged in, check role
                val role = com.example.bookbud.shared.auth.TokenManager.getRole() ?: "USER"
                if (role.equals("ADMIN", ignoreCase = true)) {
                    Intent(this, AdminDashboardActivity::class.java)
                } else {
                    Intent(this, com.example.bookbud.features.dashboard.presentation.activity.DashboardActivity::class.java)
                }
            } else {
                // User not logged in, go to onboarding
                Intent(this, GetStartedActivity::class.java)
            }
            
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            Log.e("SplashActivity", "Navigation error", e)
            // Fallback: always go to GetStarted
            startActivity(Intent(this, GetStartedActivity::class.java))
            finish()
        }
    }
}
