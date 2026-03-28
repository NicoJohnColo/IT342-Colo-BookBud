package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail: EditText = findViewById(R.id.editLoginEmail)
        val editPassword: EditText = findViewById(R.id.editLoginPassword)
        val progress: ProgressBar = findViewById(R.id.progressLogin)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val textNoAccount: TextView = findViewById(R.id.textNoAccount)

        textNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            if (!validateInputs(email, password, editEmail, editPassword)) {
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            buttonLogin.isEnabled = false

            Thread {
                val result = AuthApiClient.login(email, password)
                runOnUiThread {
                    progress.visibility = View.GONE
                    buttonLogin.isEnabled = true

                    when (result) {
                        is AuthResult.Success -> {
                            // Store tokens and username for later use
                            val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
                            prefs.edit()
                                .putString("access_token", result.accessToken)
                                .putString("refresh_token", result.refreshToken)
                                .putString("username", result.username)
                                .apply()

                            Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                        is AuthResult.Error -> {
                            Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.start()
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        editEmail: EditText,
        editPassword: EditText
    ): Boolean {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Valid email is required"
            editEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            editPassword.error = "Password is required"
            editPassword.requestFocus()
            return false
        }

        return true
    }
}
