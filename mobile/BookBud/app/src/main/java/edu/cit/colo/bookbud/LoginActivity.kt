package edu.cit.colo.bookbud

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail: EditText = findViewById(R.id.editLoginEmail)
        val editPassword: EditText = findViewById(R.id.editLoginPassword)
        val togglePassword: ImageView = findViewById(R.id.toggleLoginPassword)
        val progress: ProgressBar = findViewById(R.id.progressLogin)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val textNoAccount: TextView = findViewById(R.id.textNoAccount)
        val textForgotPassword: TextView = findViewById(R.id.textForgotPassword)

        // Password toggle
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val cursorPos = editPassword.selectionEnd
            if (isPasswordVisible) {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            editPassword.setSelection(cursorPos)
        }

        textNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        textForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        buttonLogin.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }

        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Valid email is required"; editEmail.requestFocus(); return@setOnClickListener
            }
            if (password.isEmpty()) {
                editPassword.error = "Password is required"; editPassword.requestFocus(); return@setOnClickListener
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
                            val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
                            prefs.edit()
                                .putString("access_token", result.accessToken)
                                .putString("refresh_token", result.refreshToken)
                                .putString("username", result.username)
                                .putString("user_id", result.userId)
                                .apply()
                            startActivity(Intent(this, DashboardActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                        is AuthResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
}
