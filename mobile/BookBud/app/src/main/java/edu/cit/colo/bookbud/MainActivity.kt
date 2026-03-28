package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getStartedButton: Button = findViewById(R.id.buttonGetStarted)
        val loginLink: TextView = findViewById(R.id.textLoginLink)

        getStartedButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
