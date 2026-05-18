package com.example.bookbud.features.auth.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookbud.R

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val editForgotEmail = findViewById<EditText>(R.id.editForgotEmail)
        val buttonSendReset = findViewById<Button>(R.id.buttonSendReset)
        val textSignIn = findViewById<TextView>(R.id.textSignIn)

        // Send reset link button
        buttonSendReset.setOnClickListener {
            val email = editForgotEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_SHORT).show()
                // In production, make API call to send reset email
                // For now, navigate back to login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Back to login
        textSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
