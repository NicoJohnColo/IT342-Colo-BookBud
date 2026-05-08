package edu.cit.colo.bookbud

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class GetStartedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        val logoText: TextView = findViewById(R.id.logoText)
        val tagline: TextView = findViewById(R.id.tagline)
        val getStartedButton: Button = findViewById(R.id.getStartedButton)

        // Staggered fade-in-up for logo
        val logoAlpha = ObjectAnimator.ofFloat(logoText, "alpha", 0f, 1f).setDuration(700)
        val logoTransY = ObjectAnimator.ofFloat(logoText, "translationY", 40f, 0f).apply {
            duration = 700
            interpolator = DecelerateInterpolator()
        }

        // Tagline slightly delayed
        val tagAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 200
        }
        val tagTransY = ObjectAnimator.ofFloat(tagline, "translationY", 30f, 0f).apply {
            duration = 600
            startDelay = 200
            interpolator = DecelerateInterpolator()
        }

        // Button slides up from bottom with overshoot
        val btnAlpha = ObjectAnimator.ofFloat(getStartedButton, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 450
        }
        val btnTransY = ObjectAnimator.ofFloat(getStartedButton, "translationY", 60f, 0f).apply {
            duration = 700
            startDelay = 450
            interpolator = OvershootInterpolator(1.2f)
        }

        AnimatorSet().apply {
            playTogether(logoAlpha, logoTransY, tagAlpha, tagTransY, btnAlpha, btnTransY)
            start()
        }

        // Button press animation + navigation
        getStartedButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f).scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(80).withEndAction {
                        startActivity(Intent(this, LoginActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }.start()
                }.start()
        }
    }
}
