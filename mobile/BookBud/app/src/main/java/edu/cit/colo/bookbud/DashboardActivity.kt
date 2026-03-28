package edu.cit.colo.bookbud

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textGreeting: TextView = findViewById(R.id.textGreeting)

        val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
        val username = prefs.getString("username", null)

        if (!username.isNullOrEmpty()) {
            textGreeting.text = "Hello, $username"
        }
    }
}
