package edu.cit.colo.bookbud

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ForgotPasswordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val editEmail: EditText = findViewById(R.id.editForgotEmail)
        val buttonSend: Button = findViewById(R.id.buttonSendReset)
        val textBackToLogin: TextView = findViewById(R.id.textBackToLogin)
        val textSignIn: TextView = findViewById(R.id.textSignIn)

        buttonSend.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }

        buttonSend.setOnClickListener {
            val email = editEmail.text.toString().trim()
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Valid email is required"
                editEmail.requestFocus()
                return@setOnClickListener
            }
            Toast.makeText(this, "If this account exists, we'll email you a reset link.", Toast.LENGTH_LONG).show()
        }

        textBackToLogin.setOnClickListener { navigateBack() }
        textSignIn.setOnClickListener { navigateBack() }
    }

    private fun navigateBack() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
