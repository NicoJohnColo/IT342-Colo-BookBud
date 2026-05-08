package edu.cit.colo.bookbud

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.ComponentActivity

// Splash screen entry point for the BookBud mobile app.
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val imageLogo: ImageView = findViewById(R.id.imageLogo)
        val progressSplash: ProgressBar = findViewById(R.id.progressSplash)

        // Fade in logo with scale
        val logoAlpha = ObjectAnimator.ofFloat(imageLogo, "alpha", 0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        val logoScaleX = ObjectAnimator.ofFloat(imageLogo, "scaleX", 0.8f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        val logoScaleY = ObjectAnimator.ofFloat(imageLogo, "scaleY", 0.8f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        // Fade in spinner with delay
        val spinnerAlpha = ObjectAnimator.ofFloat(progressSplash, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 400
        }

        AnimatorSet().apply {
            playTogether(logoAlpha, logoScaleX, logoScaleY, spinnerAlpha)
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = when {
                TokenManager.isAdmin(this) -> AdminDashboardActivity::class.java
                TokenManager.isLoggedIn(this) -> DashboardActivity::class.java
                else -> GetStartedActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200L)
    }
}
